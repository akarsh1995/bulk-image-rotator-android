package com.rotator.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import com.rotator.app.data.model.ImageItem
import com.rotator.app.data.model.OutputMode
import com.rotator.app.data.model.RotationAngle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ImageRepository(private val context: Context) {

    suspend fun loadImagesFromDirectory(treeUri: Uri): List<ImageItem> = withContext(Dispatchers.IO) {
        val rootDir = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val files = rootDir.listFiles()

        val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "heic", "bmp")

        files.filter { file ->
            file.isFile && (
                file.type?.startsWith("image/") == true ||
                    imageExtensions.any { ext -> file.name?.lowercase()?.endsWith(".$ext") == true }
                )
        }.map { doc ->
            ImageItem(
                uri = doc.uri,
                name = doc.name ?: "Unknown",
                dateModified = doc.lastModified(),
                size = doc.length(),
                mimeType = doc.type ?: "image/jpeg"
            )
        }.sortedByDescending { it.dateModified }
    }

    suspend fun rotateImage(
        item: ImageItem,
        angle: RotationAngle,
        mode: OutputMode,
        sourceTreeUri: Uri,
        outputTreeUri: Uri?
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var sourceBitmap: Bitmap? = null
        var rotatedBitmap: Bitmap? = null
        var tempFile: File? = null

        try {
            // Read input stream and save temporary copy to inspect/preserve EXIF easily
            tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_${item.name}")
            context.contentResolver.openInputStream(item.uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open input stream for ${item.name}"))

            // Read original EXIF
            val originalExif = try {
                ExifInterface(tempFile.absolutePath)
            } catch (e: Exception) {
                null
            }

            // Determine intrinsic EXIF rotation degrees if present
            val exifOrientation = originalExif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val exifRotation = when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            // Decode bitmap from file
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            sourceBitmap = BitmapFactory.decodeFile(tempFile.absolutePath, options)
                ?: return@withContext Result.failure(Exception("Failed to decode bitmap for ${item.name}"))

            // Total rotation = intrinsic EXIF rotation + user requested rotation
            val totalDegrees = (exifRotation + angle.degrees) % 360f

            val matrix = Matrix()
            if (totalDegrees != 0f) {
                matrix.postRotate(totalDegrees)
            }

            rotatedBitmap = if (totalDegrees != 0f) {
                Bitmap.createBitmap(
                    sourceBitmap,
                    0,
                    0,
                    sourceBitmap.width,
                    sourceBitmap.height,
                    matrix,
                    true
                )
            } else {
                sourceBitmap
            }

            // Determine compression format and quality
            val isPng = item.name.lowercase().endsWith(".png") || item.mimeType == "image/png"
            val isWebp = item.name.lowercase().endsWith(".webp") || item.mimeType == "image/webp"
            val compressFormat = when {
                isPng -> Bitmap.CompressFormat.PNG
                isWebp -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
            val quality = if (isPng) 100 else 95

            // Write output to target URI
            val targetUri: Uri = when (mode) {
                OutputMode.OVERWRITE -> {
                    context.contentResolver.openOutputStream(item.uri, "wt")?.use { out ->
                        rotatedBitmap.compress(compressFormat, quality, out)
                    } ?: return@withContext Result.failure(Exception("Cannot open output stream for overwrite"))
                    item.uri
                }

                OutputMode.SAVE_COPY -> {
                    val targetTreeUri = outputTreeUri ?: sourceTreeUri
                    val targetDir = DocumentFile.fromTreeUri(context, targetTreeUri)
                        ?: return@withContext Result.failure(Exception("Cannot access destination folder"))

                    val dotIndex = item.name.lastIndexOf('.')
                    val baseName = if (dotIndex > 0) item.name.substring(0, dotIndex) else item.name
                    val extension = if (dotIndex > 0) item.name.substring(dotIndex) else ".jpg"
                    val newName = "${baseName}_rot${angle.degrees}$extension"

                    val newDoc = targetDir.createFile(item.mimeType, newName)
                        ?: return@withContext Result.failure(Exception("Cannot create file $newName in folder"))

                    context.contentResolver.openOutputStream(newDoc.uri, "wt")?.use { out ->
                        rotatedBitmap.compress(compressFormat, quality, out)
                    } ?: return@withContext Result.failure(Exception("Cannot write to created file"))

                    newDoc.uri
                }
            }

            // Write updated EXIF with ORIENTATION_NORMAL since pixels are physically rotated
            try {
                context.contentResolver.openFileDescriptor(targetUri, "rw")?.use { pfd ->
                    val targetExif = ExifInterface(pfd.fileDescriptor)
                    originalExif?.let { copyExifAttributes(it, targetExif) }
                    targetExif.setAttribute(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL.toString()
                    )
                    targetExif.setAttribute(
                        ExifInterface.TAG_IMAGE_WIDTH,
                        rotatedBitmap.width.toString()
                    )
                    targetExif.setAttribute(
                        ExifInterface.TAG_IMAGE_LENGTH,
                        rotatedBitmap.height.toString()
                    )
                    targetExif.saveAttributes()
                }
            } catch (ignored: Exception) {
                // EXIF writing may not be supported on all formats (e.g. PNG)
            }

            Result.success(targetUri)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            if (rotatedBitmap != sourceBitmap) {
                rotatedBitmap?.recycle()
            }
            sourceBitmap?.recycle()
            tempFile?.delete()
        }
    }

    private fun copyExifAttributes(source: ExifInterface, destination: ExifInterface) {
        val tags = listOf(
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_DATESTAMP
        )
        for (tag in tags) {
            val value = source.getAttribute(tag)
            if (value != null) {
                destination.setAttribute(tag, value)
            }
        }
    }
}

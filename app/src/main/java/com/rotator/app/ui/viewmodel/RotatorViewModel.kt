package com.rotator.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import com.rotator.app.data.model.OutputMode
import com.rotator.app.data.model.RotationAngle
import com.rotator.app.data.repository.ImageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RotatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImageRepository(application.applicationContext)
    private val vibrator = getVibratorService(application)

    private val _uiState = MutableStateFlow(RotatorUiState())
    val uiState: StateFlow<RotatorUiState> = _uiState.asStateFlow()

    private var messageClearJob: Job? = null
    private var rotationJob: Job? = null

    // For drag-to-select: stores selection state at drag start
    private var dragInitialSelection: Set<Int> = emptySet()
    private var dragAnchorIndex: Int? = null

    fun setDirectory(uri: Uri) {
        val context = getApplication<Application>().applicationContext
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (ignored: Exception) {}

        val doc = DocumentFile.fromTreeUri(context, uri)
        val name = doc?.name ?: uri.lastPathSegment ?: "Folder"

        _uiState.update {
            it.copy(
                directoryUri = uri,
                directoryName = name,
                selectedIndices = emptySet()
            )
        }
        loadImages()
    }

    fun setOutputDirectory(uri: Uri) {
        val context = getApplication<Application>().applicationContext
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (ignored: Exception) {}

        val doc = DocumentFile.fromTreeUri(context, uri)
        val name = doc?.name ?: uri.lastPathSegment ?: "Output Folder"

        _uiState.update {
            it.copy(
                outputDirectoryUri = uri,
                outputDirectoryName = name
            )
        }
    }

    fun refreshImages() {
        loadImages()
    }

    private fun loadImages() {
        val uri = _uiState.value.directoryUri ?: return
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val imageList = repository.loadImagesFromDirectory(uri)
            _uiState.update {
                it.copy(
                    images = imageList,
                    isLoading = false,
                    selectedIndices = emptySet()
                )
            }
        }
    }

    fun toggleSelection(index: Int) {
        performHapticFeedback()
        _uiState.update { state ->
            val updated = state.selectedIndices.toMutableSet()
            if (updated.contains(index)) {
                updated.remove(index)
            } else {
                updated.add(index)
            }
            state.copy(selectedIndices = updated)
        }
    }

    fun startDragSelection(anchorIndex: Int) {
        performHapticFeedback()
        dragAnchorIndex = anchorIndex
        dragInitialSelection = _uiState.value.selectedIndices
        val newSelection = dragInitialSelection.toMutableSet().apply {
            add(anchorIndex)
        }
        _uiState.update { it.copy(selectedIndices = newSelection) }
    }

    fun updateDragSelection(currentIndex: Int) {
        val anchor = dragAnchorIndex ?: return
        val range = if (anchor <= currentIndex) anchor..currentIndex else currentIndex..anchor
        val newSelection = dragInitialSelection.toMutableSet()
        newSelection.addAll(range)

        if (newSelection != _uiState.value.selectedIndices) {
            performLightHapticFeedback()
            _uiState.update { it.copy(selectedIndices = newSelection) }
        }
    }

    fun endDragSelection() {
        dragAnchorIndex = null
        dragInitialSelection = emptySet()
    }

    fun selectAll() {
        val total = _uiState.value.images.size
        _uiState.update {
            it.copy(selectedIndices = (0 until total).toSet())
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedIndices = emptySet())
        }
    }

    fun setAngle(angle: RotationAngle) {
        _uiState.update { it.copy(selectedAngle = angle) }
    }

    fun setOutputMode(mode: OutputMode) {
        _uiState.update { it.copy(outputMode = mode) }
    }

    fun setShowRotateSheet(show: Boolean) {
        _uiState.update { it.copy(showRotateSheet = show) }
    }

    fun cancelRotation() {
        rotationJob?.cancel()
        _uiState.update {
            it.copy(
                isRotating = false,
                statusMessage = "Rotation cancelled"
            )
        }
        scheduleMessageDismiss()
    }

    fun startRotation() {
        val state = _uiState.value
        val sourceDirUri = state.directoryUri ?: return
        val selectedImages = state.selectedIndices
            .filter { it in state.images.indices }
            .map { state.images[it] }

        if (selectedImages.isEmpty()) return

        _uiState.update {
            it.copy(
                isRotating = true,
                showRotateSheet = false,
                rotationProgress = 0f,
                rotationCurrent = 0,
                rotationTotal = selectedImages.size,
                currentRotatingFileName = selectedImages.first().name
            )
        }

        rotationJob = viewModelScope.launch {
            var successCount = 0
            val total = selectedImages.size
            val angle = state.selectedAngle
            val mode = state.outputMode
            val outputUri = state.outputDirectoryUri

            try {
                for ((index, item) in selectedImages.withIndex()) {
                    _uiState.update {
                        it.copy(
                            rotationCurrent = index + 1,
                            rotationProgress = (index + 1).toFloat() / total,
                            currentRotatingFileName = item.name
                        )
                    }

                    val result = repository.rotateImage(
                        item = item,
                        angle = angle,
                        mode = mode,
                        sourceTreeUri = sourceDirUri,
                        outputTreeUri = outputUri
                    )

                    if (result.isSuccess) {
                        successCount++
                    }
                }

                // Invalidate Coil memory & disk cache to reflect rotated images
                val context = getApplication<Application>().applicationContext
                context.imageLoader.memoryCache?.clear()

                _uiState.update {
                    it.copy(
                        isRotating = false,
                        selectedIndices = emptySet(),
                        statusMessage = "Rotated $successCount of $total images (${angle.label})"
                    )
                }
                scheduleMessageDismiss()
                // Reload images to show the updated directory state
                loadImages()
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(
                        isRotating = false,
                        statusMessage = "Rotation cancelled ($successCount completed)"
                    )
                }
                scheduleMessageDismiss()
                loadImages()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRotating = false,
                        statusMessage = "Error rotating images: ${e.localizedMessage}"
                    )
                }
                scheduleMessageDismiss()
                loadImages()
            }
        }
    }

    private fun scheduleMessageDismiss() {
        messageClearJob?.cancel()
        messageClearJob = viewModelScope.launch {
            delay(3500)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }

    private fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (ignored: Exception) {}
    }

    private fun performLightHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(10, 50))
            }
        } catch (ignored: Exception) {}
    }

    private fun getVibratorService(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

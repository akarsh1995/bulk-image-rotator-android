package com.rotator.app.data.model

import android.net.Uri

data class ImageItem(
    val uri: Uri,
    val name: String,
    val dateModified: Long = 0L,
    val size: Long = 0L,
    val mimeType: String = "image/jpeg",
    val width: Int = 0,
    val height: Int = 0
)

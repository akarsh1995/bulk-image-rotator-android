package com.rotator.app.ui.viewmodel

import android.net.Uri
import com.rotator.app.data.model.ImageItem
import com.rotator.app.data.model.OutputMode
import com.rotator.app.data.model.RotationAngle

data class RotatorUiState(
    val images: List<ImageItem> = emptyList(),
    val selectedIndices: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isRotating: Boolean = false,
    val rotationProgress: Float = 0f,
    val rotationCurrent: Int = 0,
    val rotationTotal: Int = 0,
    val currentRotatingFileName: String? = null,
    val selectedAngle: RotationAngle = RotationAngle.CW_90,
    val outputMode: OutputMode = OutputMode.SAVE_COPY,
    val directoryUri: Uri? = null,
    val directoryName: String? = null,
    val outputDirectoryUri: Uri? = null,
    val outputDirectoryName: String? = null,
    val statusMessage: String? = null,
    val showRotateSheet: Boolean = false
) {
    val selectionMode: Boolean get() = selectedIndices.isNotEmpty()
    val selectedCount: Int get() = selectedIndices.size
}

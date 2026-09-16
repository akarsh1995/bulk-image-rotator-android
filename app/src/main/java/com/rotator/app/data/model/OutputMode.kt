package com.rotator.app.data.model

enum class OutputMode(val label: String, val description: String) {
    OVERWRITE("Overwrite", "Replace the original image files directly"),
    SAVE_COPY("Save Copy", "Save newly rotated images with a suffix")
}

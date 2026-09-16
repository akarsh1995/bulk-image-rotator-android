# Bulk Image Rotator Android App

A modern Android application built with Jetpack Compose that allows users to pick a directory from device storage, select multiple images using a Google Photos-style drag-to-select gesture, and rotate them in bulk (90°, 180°, 270°) with full EXIF preservation.

## Features

- **SAF Directory Access**: Select any folder on device storage using Android's Storage Access Framework.
- **Drag-to-Select**: Long-press to initiate selection mode and drag across images to multi-select, including viewport edge auto-scrolling and haptic feedback.
- **Bulk Rotation**: Rotate selected images by 90°, 180°, or 270° clockwise.
- **EXIF Preservation**: Preserves original EXIF tags (camera metadata, timestamps, GPS) while updating orientation and dimensions.
- **Output Flexibility**: Choose between in-place overwrite or saving rotated copies to a chosen destination directory.
- **Modern Architecture**: MVVM, AndroidViewModel, Kotlin Coroutines & StateFlow, Jetpack Compose, Material 3 Dark Theme, Coil.

## Tech Stack

- **Kotlin** 2.1.0
- **Jetpack Compose** (Compose BOM 2025.01.00)
- **Material 3**
- **Coil** 2.7.0
- **AndroidX ExifInterface** & **DocumentFile**
- **Gradle 9.4.1** with Version Catalog (`libs.versions.toml`)

## Building

```bash
./gradlew assembleDebug
```
The resulting APK will be placed in `app/build/outputs/apk/debug/app-debug.apk`.

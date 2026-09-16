package com.rotator.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotator.app.ui.components.DirectoryHeader
import com.rotator.app.ui.components.EmptyDirectoryState
import com.rotator.app.ui.components.ImageGrid
import com.rotator.app.ui.components.RotateBottomSheet
import com.rotator.app.ui.components.RotationProgressOverlay
import com.rotator.app.ui.components.SelectionTopBar
import com.rotator.app.ui.theme.BackgroundDark
import com.rotator.app.ui.theme.PrimaryBlue
import com.rotator.app.ui.theme.SurfaceDark
import com.rotator.app.ui.theme.SurfaceElevated
import com.rotator.app.ui.theme.TextPrimary
import com.rotator.app.ui.theme.TextSecondary
import com.rotator.app.ui.viewmodel.RotatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotatorScreen(
    viewModel: RotatorViewModel,
    onPickDirectory: () -> Unit,
    onPickOutputDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.selectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedCount,
                    totalCount = uiState.images.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() },
                    onRotateClick = { viewModel.setShowRotateSheet(true) }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "Image Rotator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = onPickDirectory) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Open folder",
                                tint = PrimaryBlue
                            )
                        }
                        if (uiState.directoryUri != null) {
                            IconButton(onClick = { viewModel.refreshImages() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh folder",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceDark
                    )
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.selectionMode,
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setShowRotateSheet(true) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Rotate ${uiState.selectedCount}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = PrimaryBlue,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Folder bar if folder is selected
            if (uiState.directoryUri != null) {
                DirectoryHeader(
                    directoryName = uiState.directoryName,
                    imageCount = uiState.images.size,
                    onChangeDirectory = onPickDirectory
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }

                    uiState.directoryUri == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyDirectoryState(onSelectDirectory = onPickDirectory)
                        }
                    }

                    uiState.images.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No images found",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This folder does not contain any supported images",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }

                    else -> {
                        ImageGrid(
                            images = uiState.images,
                            selectedIndices = uiState.selectedIndices,
                            isSelectionModeActive = uiState.selectionMode,
                            onToggleSelect = { index -> viewModel.toggleSelection(index) },
                            onDragStart = { index -> viewModel.startDragSelection(index) },
                            onDragUpdate = { index -> viewModel.updateDragSelection(index) },
                            onDragEnd = { viewModel.endDragSelection() }
                        )
                    }
                }
            }
        }
    }

    // Rotate Bottom Sheet
    if (uiState.showRotateSheet) {
        RotateBottomSheet(
            selectedCount = uiState.selectedCount,
            currentAngle = uiState.selectedAngle,
            currentMode = uiState.outputMode,
            outputDirectoryName = uiState.outputDirectoryName,
            onSelectAngle = { viewModel.setAngle(it) },
            onSelectMode = { viewModel.setOutputMode(it) },
            onPickOutputFolder = onPickOutputDirectory,
            onConfirmRotate = { viewModel.startRotation() },
            onDismiss = { viewModel.setShowRotateSheet(false) }
        )
    }

    // Rotation Progress Dialog
    if (uiState.isRotating) {
        RotationProgressOverlay(
            current = uiState.rotationCurrent,
            total = uiState.rotationTotal,
            progress = uiState.rotationProgress,
            currentFileName = uiState.currentRotatingFileName,
            onCancel = { viewModel.cancelRotation() }
        )
    }
}

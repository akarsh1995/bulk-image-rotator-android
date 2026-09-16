package com.rotator.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.rotator.app.data.model.ImageItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun ImageGrid(
    images: List<ImageItem>,
    selectedIndices: Set<Int>,
    isSelectionModeActive: Boolean,
    onToggleSelect: (Int) -> Unit,
    onDragStart: (Int) -> Unit,
    onDragUpdate: (Int) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState()
) {
    val coroutineScope = rememberCoroutineScope()
    var currentPointerY by remember { mutableFloatStateOf(-1f) }
    var gridHeight by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Auto-scroll when dragging near top or bottom edges (like Google Photos)
    LaunchedEffect(isDragging, currentPointerY, gridHeight) {
        if (!isDragging || gridHeight <= 0f) return@LaunchedEffect

        val edgeThreshold = 120f
        while (isActive && isDragging) {
            val scrollDelta = when {
                currentPointerY in 0f..edgeThreshold -> {
                    // Scroll up faster the closer to the top edge
                    val ratio = 1f - (currentPointerY / edgeThreshold).coerceIn(0f, 1f)
                    -(10f + ratio * 25f)
                }
                currentPointerY in (gridHeight - edgeThreshold)..gridHeight -> {
                    // Scroll down faster the closer to the bottom edge
                    val ratio = ((currentPointerY - (gridHeight - edgeThreshold)) / edgeThreshold).coerceIn(0f, 1f)
                    10f + ratio * 25f
                }
                else -> 0f
            }

            if (scrollDelta != 0f) {
                gridState.scrollBy(scrollDelta)
                // Also update drag item hit test while scrolling
                val visible = gridState.layoutInfo.visibleItemsInfo
                val hitItem = visible.firstOrNull { item ->
                    currentPointerY >= item.offset.y && currentPointerY <= item.offset.y + item.size.height
                }
                hitItem?.let { onDragUpdate(it.index) }
            }

            delay(16) // ~60fps scroll step
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                gridHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(images) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        isDragging = true
                        currentPointerY = offset.y
                        val hitItem = findItemIndexAtOffset(gridState, offset)
                        if (hitItem != null && hitItem in images.indices) {
                            onDragStart(hitItem)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPointerY = change.position.y
                        val hitItem = findItemIndexAtOffset(gridState, change.position)
                        if (hitItem != null && hitItem in images.indices) {
                            onDragUpdate(hitItem)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        currentPointerY = -1f
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        currentPointerY = -1f
                        onDragEnd()
                    }
                )
            }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = images,
                key = { _, item -> item.uri.toString() }
            ) { index, item ->
                ImageThumbnail(
                    item = item,
                    isSelected = selectedIndices.contains(index),
                    isSelectionModeActive = isSelectionModeActive,
                    onClick = {
                        if (isSelectionModeActive) {
                            onToggleSelect(index)
                        } else {
                            // Tap on image when not in selection mode can start selection mode
                            onToggleSelect(index)
                        }
                    },
                    onLongClick = {
                        // Long press when not dragging can toggle
                        onToggleSelect(index)
                    }
                )
            }
        }
    }
}

private fun findItemIndexAtOffset(gridState: LazyGridState, offset: Offset): Int? {
    val visibleItems = gridState.layoutInfo.visibleItemsInfo
    return visibleItems.firstOrNull { item ->
        offset.x >= item.offset.x &&
            offset.x <= item.offset.x + item.size.width &&
            offset.y >= item.offset.y &&
            offset.y <= item.offset.y + item.size.height
    }?.index
}

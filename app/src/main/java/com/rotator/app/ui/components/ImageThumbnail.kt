package com.rotator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rotator.app.data.model.ImageItem
import com.rotator.app.ui.theme.CheckmarkWhite
import com.rotator.app.ui.theme.PrimaryBlue
import com.rotator.app.ui.theme.SelectionBadge
import com.rotator.app.ui.theme.SelectionOverlay
import com.rotator.app.ui.theme.SurfaceContainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageThumbnail(
    item: ImageItem,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paddingAnim by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        label = "thumbnailPadding"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingAnim)
                .clip(RoundedCornerShape(if (isSelected) 8.dp else 4.dp))
                .background(SurfaceContainer)
                .then(
                    if (isSelected) {
                        Modifier.border(2.5.dp, PrimaryBlue, RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    }
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim overlay when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SelectionOverlay)
                )
            }

            // Selection indicator badge
            if (isSelectionModeActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(SelectionBadge),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = CheckmarkWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // Unselected hollow circle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

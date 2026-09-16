package com.rotator.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotator.app.data.model.OutputMode
import com.rotator.app.data.model.RotationAngle
import com.rotator.app.ui.theme.BorderSubtle
import com.rotator.app.ui.theme.PrimaryBlue
import com.rotator.app.ui.theme.SecondaryTeal
import com.rotator.app.ui.theme.SurfaceContainer
import com.rotator.app.ui.theme.SurfaceDark
import com.rotator.app.ui.theme.SurfaceElevated
import com.rotator.app.ui.theme.TextPrimary
import com.rotator.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotateBottomSheet(
    selectedCount: Int,
    currentAngle: RotationAngle,
    currentMode: OutputMode,
    outputDirectoryName: String?,
    onSelectAngle: (RotationAngle) -> Unit,
    onSelectMode: (OutputMode) -> Unit,
    onPickOutputFolder: () -> Unit,
    onConfirmRotate: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextSecondary.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Bulk Rotate Images",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = "Applying to $selectedCount selected image${if (selectedCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
            )

            // Section 1: Angle Selection
            Text(
                text = "Rotation Angle (Clockwise)",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RotationAngleCard(
                    angle = RotationAngle.CW_90,
                    icon = Icons.Default.Rotate90DegreesCw,
                    isSelected = currentAngle == RotationAngle.CW_90,
                    onSelect = { onSelectAngle(RotationAngle.CW_90) },
                    modifier = Modifier.weight(1f)
                )

                RotationAngleCard(
                    angle = RotationAngle.CW_180,
                    icon = Icons.Default.ChangeCircle,
                    isSelected = currentAngle == RotationAngle.CW_180,
                    onSelect = { onSelectAngle(RotationAngle.CW_180) },
                    modifier = Modifier.weight(1f)
                )

                RotationAngleCard(
                    angle = RotationAngle.CW_270,
                    icon = Icons.Default.Replay,
                    isSelected = currentAngle == RotationAngle.CW_270,
                    onSelect = { onSelectAngle(RotationAngle.CW_270) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Output Mode
            Text(
                text = "Save Mode",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutputModeOption(
                mode = OutputMode.SAVE_COPY,
                currentMode = currentMode,
                onSelect = { onSelectMode(OutputMode.SAVE_COPY) }
            )

            if (currentMode == OutputMode.SAVE_COPY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = outputDirectoryName ?: "Same as source folder",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onPickOutputFolder,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SecondaryTeal.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryTeal),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("Pick Folder", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutputModeOption(
                mode = OutputMode.OVERWRITE,
                currentMode = currentMode,
                onSelect = { onSelectMode(OutputMode.OVERWRITE) }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Confirm Button
            Button(
                onClick = onConfirmRotate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rotate $selectedCount Image${if (selectedCount == 1) "" else "s"} (${currentAngle.label})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RotationAngleCard(
    angle: RotationAngle,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect),
        color = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else SurfaceElevated,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) PrimaryBlue else BorderSubtle
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryBlue else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = angle.label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryBlue else TextPrimary
                )
            )
        }
    }
}

@Composable
private fun OutputModeOption(
    mode: OutputMode,
    currentMode: OutputMode,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect),
        color = if (mode == currentMode) SurfaceContainer else SurfaceElevated,
        border = BorderStroke(
            1.dp,
            if (mode == currentMode) PrimaryBlue.copy(alpha = 0.5f) else BorderSubtle
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = mode == currentMode,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PrimaryBlue,
                    unselectedColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )
            }
        }
    }
}

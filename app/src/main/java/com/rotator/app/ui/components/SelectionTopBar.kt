package com.rotator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rotator.app.ui.theme.BackgroundDark
import com.rotator.app.ui.theme.PrimaryBlue
import com.rotator.app.ui.theme.SurfaceElevated
import com.rotator.app.ui.theme.TextPrimary
import com.rotator.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onRotateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceElevated,
        shadowElevation = 4.dp
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selection",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                TextButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onRotateClick,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rotate")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceElevated
            )
        )
    }
}

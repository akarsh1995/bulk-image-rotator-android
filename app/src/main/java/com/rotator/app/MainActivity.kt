package com.rotator.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rotator.app.ui.RotatorScreen
import com.rotator.app.ui.theme.BackgroundDark
import com.rotator.app.ui.theme.ImageRotatorTheme
import com.rotator.app.ui.viewmodel.RotatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RotatorViewModel by viewModels()

    private val pickDirectoryLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setDirectory(it)
        }
    }

    private val pickOutputDirectoryLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setOutputDirectory(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageRotatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    RotatorScreen(
                        viewModel = viewModel,
                        onPickDirectory = {
                            pickDirectoryLauncher.launch(null)
                        },
                        onPickOutputDirectory = {
                            pickOutputDirectoryLauncher.launch(null)
                        }
                    )
                }
            }
        }
    }
}

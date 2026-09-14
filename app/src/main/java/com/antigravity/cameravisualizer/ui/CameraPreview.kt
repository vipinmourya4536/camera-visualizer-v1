package com.antigravity.cameravisualizer.ui

import android.view.ViewGroup
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.antigravity.cameravisualizer.camera.CameraEngine

@Composable
fun CameraPreview(
    cameraEngine: CameraEngine,
    lensFacing: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            cameraEngine.bindCameraUseCases(
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = view.surfaceProvider,
                lensFacing = lensFacing
            )
        }
    )
}

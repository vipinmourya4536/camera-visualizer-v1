package com.antigravity.cameravisualizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.antigravity.cameravisualizer.camera.CameraViewModel
import com.antigravity.cameravisualizer.ui.CameraScreen

class MainActivity : ComponentActivity() {

    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Target modern Android edge-to-edge behavior
        enableEdgeToEdge()

        setContent {
            CameraScreen(viewModel = cameraViewModel)
        }
    }
}

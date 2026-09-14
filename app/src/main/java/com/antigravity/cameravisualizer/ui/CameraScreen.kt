package com.antigravity.cameravisualizer.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.cameravisualizer.camera.CameraViewModel
import com.antigravity.cameravisualizer.camera.PermissionState
import com.antigravity.cameravisualizer.haptics.HapticController
import com.antigravity.cameravisualizer.processing.ToneHueMode
import com.antigravity.cameravisualizer.styles.StyleLibrary
import kotlinx.coroutines.delay

@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val captureState by viewModel.captureState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val hapticController = remember { HapticController(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (isGranted) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (cameraState.permissionState == PermissionState.GRANTED) {
        if (cameraState.isInitialized) {
            CameraContent(
                viewModel = viewModel,
                hapticController = hapticController
            )
        }
    } else {
        PermissionScreen(onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        })
    }
}

@Composable
fun CameraContent(
    viewModel: CameraViewModel,
    hapticController: HapticController
) {
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val captureState by viewModel.captureState.collectAsStateWithLifecycle()

    var showPresetName by remember { mutableStateOf(false) }

    LaunchedEffect(processingState.presetId) {
        showPresetName = true
        delay(1500)
        showPresetName = false
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount > 20) { // Swipe right (previous)
                    val currentIndex = StyleLibrary.styles.indexOfFirst { it.id == processingState.presetId }
                    val newIndex = if (currentIndex - 1 < 0) StyleLibrary.styles.size - 1 else currentIndex - 1
                    viewModel.setPreset(StyleLibrary.styles[newIndex].id)
                    hapticController.subtleTick()
                } else if (dragAmount < -20) { // Swipe left (next)
                    val currentIndex = StyleLibrary.styles.indexOfFirst { it.id == processingState.presetId }
                    val newIndex = (currentIndex + 1) % StyleLibrary.styles.size
                    viewModel.setPreset(StyleLibrary.styles[newIndex].id)
                    hapticController.subtleTick()
                }
            }
        }
    ) {
        // Camera Preview
        CameraPreview(
            cameraEngine = viewModel.cameraEngine,
            lensFacing = cameraState.cameraFacing,
            modifier = Modifier.fillMaxSize()
        )

        // Visual overlay for style preview (prototype level)
        if (processingState.presetId == "pop-color") {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(0x22FFAA00)))
        }

        // Top Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("PRESET", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = StyleLibrary.styles.find { it.id == processingState.presetId }?.name ?: "STANDARD",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Center Preset Name Flash
        if (showPresetName) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = StyleLibrary.styles.find { it.id == processingState.presetId }?.name ?: "",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }

        // Bottom Controls
        FloatingControlDock(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tone/Hue Pad
                ToneHuePad(
                    mode = processingState.mode,
                    padX = processingState.padX,
                    padY = processingState.padY,
                    onValueChange = { x, y -> viewModel.setToneHue(x, y) },
                    onModeToggle = { /* Not fully implemented mode switch in VM yet but UI updates */ }
                )

                // Shutter
                ShutterButton(
                    onClick = {
                        hapticController.shutterClick()
                        viewModel.capturePhoto()
                    }
                )

                // Camera Switch
                Button(
                    onClick = {
                        hapticController.subtleTick()
                        viewModel.toggleCamera()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("SWITCH", color = Color.Black)
                }
            }
        }

        // Review Overlay
        if (captureState.reviewOpen && captureState.lastPhotoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "CAPTURED: ${StyleLibrary.styles.find { it.id == processingState.presetId }?.name}",
                        color = Color.White,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(onClick = { viewModel.closeReview() }) {
                            Text("RETAKE")
                        }
                        Button(onClick = { viewModel.closeReview() }) {
                            Text("SAVE")
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Camera access is needed to take photographs.",
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Enable Camera", color = Color.White)
            }
        }
    }
}

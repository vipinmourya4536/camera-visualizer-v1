package com.antigravity.cameravisualizer.capture

import android.net.Uri

data class CaptureState(
    val isCapturing: Boolean = false,
    val lastPhotoUri: Uri? = null,
    val reviewOpen: Boolean = false,
    val processingProgress: Float = 0f
)

package com.antigravity.cameravisualizer.camera

data class CameraState(
    val cameraFacing: Int = 1, // 1 for Rear (default), 0 for Front
    val activeCameraId: String? = null,
    val physicalCameraId: String? = null,
    val activeLens: Float = 1.0f,
    val availableLenses: List<Float> = listOf(0.5f, 1.0f, 2.0f, 3.0f, 5.0f),
    val zoomRatio: Float = 1.0f,
    val zoomRange: ClosedFloatingPointRange<Float> = 1.0f..10.0f,
    val isInitialized: Boolean = false,
    val isReady: Boolean = false,
    val permissionState: PermissionState = PermissionState.UNKNOWN
)

enum class PermissionState {
    UNKNOWN,
    REQUESTING,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    UNAVAILABLE
}

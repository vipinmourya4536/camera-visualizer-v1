package com.antigravity.cameravisualizer.processing

data class ProcessingState(
    val presetId: String = "standard",
    val intensity: Float = 0.5f,
    val mode: ToneHueMode = ToneHueMode.TONE,
    val padX: Float = 0.5f,
    val padY: Float = 0.5f,
    val exposureCompensation: Float = 0f
)

enum class ToneHueMode {
    TONE,
    HUE
}

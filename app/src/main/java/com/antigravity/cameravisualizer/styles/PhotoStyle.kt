package com.antigravity.cameravisualizer.styles

data class PhotoStyle(
    val id: String,
    val name: String
)

object StyleLibrary {
    val styles = listOf(
        PhotoStyle("standard", "STANDARD"),
        PhotoStyle("warm-cinema", "WARM CINEMA"),
        PhotoStyle("cool-studio", "COOL STUDIO"),
        PhotoStyle("high-contrast", "HIGH CONTRAST"),
        PhotoStyle("faded-print", "FADED PRINT"),
        PhotoStyle("monochrome", "MONOCHROME"),
        PhotoStyle("golden-hour", "GOLDEN HOUR"),
        PhotoStyle("soft-portrait", "SOFT PORTRAIT"),
        PhotoStyle("street", "STREET"),
        PhotoStyle("food", "FOOD"),
        PhotoStyle("pop-color", "POP COLOR")
    )
}

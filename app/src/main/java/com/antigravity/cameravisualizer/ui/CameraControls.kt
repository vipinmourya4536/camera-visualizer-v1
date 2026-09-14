package com.antigravity.cameravisualizer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cameravisualizer.processing.ToneHueMode

@Composable
fun ShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFFE89A3C)) // Amber color
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFE89A3C).copy(alpha = 0.8f))
        )
    }
}

@Composable
fun ToneHuePad(
    mode: ToneHueMode,
    padX: Float,
    padY: Float,
    onValueChange: (Float, Float) -> Unit,
    onModeToggle: (ToneHueMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF222222))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (padX + dragAmount.x / size.width).coerceIn(0f, 1f)
                        val newY = (padY + dragAmount.y / size.height).coerceIn(0f, 1f)
                        onValueChange(newX, newY)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cursorX = padX * size.width
                val cursorY = padY * size.height
                drawCircle(
                    color = Color(0xFFE89A3C),
                    radius = 8.dp.toPx(),
                    center = Offset(cursorX, cursorY)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "TONE",
                color = if (mode == ToneHueMode.TONE) Color(0xFFE89A3C) else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onModeToggle(ToneHueMode.TONE) }
            )
            Text(
                text = "HUE",
                color = if (mode == ToneHueMode.HUE) Color(0xFFE89A3C) else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onModeToggle(ToneHueMode.HUE) }
            )
        }
    }
}

@Composable
fun FloatingControlDock(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xFFF5F5F0).copy(alpha = 0.95f))
            .padding(24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

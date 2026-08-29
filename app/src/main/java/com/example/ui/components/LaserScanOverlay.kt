package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SurfaceCommand
import com.example.ui.theme.TextCyan

@Composable
fun LaserScanOverlay(
    isScanning: Boolean,
    statusText: String = "AI NEURAL FORENSIC EXTRACTION IN PROGRESS...",
    modifier: Modifier = Modifier
) {
    if (!isScanning) return

    val infiniteTransition = rememberInfiniteTransition(label = "laser_scan")
    val laserYFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser_y"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCommand.copy(alpha = 0.95f))
            .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .testTag("laser_scan_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Matrix Grid lines
            val cols = 8
            val rows = 6
            for (c in 0..cols) {
                val x = w * (c.toFloat() / cols)
                drawLine(
                    color = NeonCyan.copy(alpha = 0.08f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (r in 0..rows) {
                val y = h * (r.toFloat() / rows)
                drawLine(
                    color = NeonCyan.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Radar Pulse Concentric Circles in Center
            val center = Offset(w / 2, h / 2)
            drawCircle(
                color = NeonCyan.copy(alpha = pulseAlpha * 0.2f),
                radius = 70.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = NeonEmerald.copy(alpha = pulseAlpha * 0.4f),
                radius = 45.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Scanning Laser Line with Glow
            val currentLaserY = laserYFraction * h
            val laserGlowHeight = 24.dp.toPx()

            // Laser ambient gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.35f), Color.Transparent),
                    startY = (currentLaserY - laserGlowHeight).coerceAtLeast(0f),
                    endY = (currentLaserY + laserGlowHeight).coerceAtMost(h)
                ),
                topLeft = Offset(0f, (currentLaserY - laserGlowHeight).coerceAtLeast(0f)),
                size = androidx.compose.ui.geometry.Size(w, laserGlowHeight * 2)
            )

            // High intensity laser sharp beam
            drawLine(
                color = NeonEmerald,
                start = Offset(0f, currentLaserY),
                end = Offset(w, currentLaserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NeonBadge(text = "OPTICAL FORENSIC PARSER", color = NeonCyan, pulse = true)
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "DECODING LINE ITEMS & Surcharges...",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NeonEmerald,
                    fontSize = 10.sp
                )
            )
        }
    }
}

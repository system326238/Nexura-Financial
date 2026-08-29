package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Currency
import com.example.ui.theme.SurfaceCommand
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class DonutSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerTitle: String,
    centerValue: String,
    currency: Currency,
    modifier: Modifier = Modifier,
    chartSize: Dp = 160.dp,
    strokeWidth: Dp = 22.dp
) {
    val total = slices.sumOf { it.value }.coerceAtLeast(1.0)

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("donut_chart_container"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Chart Canvas
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                val strokePx = strokeWidth.toPx()
                val radius = (size.minDimension - strokePx) / 2
                val center = Offset(size.width / 2, size.height / 2)
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = Color(0x1AFFFFFF),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx)
                )

                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = ((slice.value / total) * 360f * animatedProgress.value).toFloat()
                    if (sweep > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweep
                }
            }

            // Center Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = centerTitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = centerValue,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // Legend Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.take(5).forEach { slice ->
                val percentage = (slice.value / total) * 100
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(slice.color, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = slice.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = String.format("%.0f%%", percentage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = slice.color,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

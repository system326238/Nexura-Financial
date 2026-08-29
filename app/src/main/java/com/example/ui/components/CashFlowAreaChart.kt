package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Currency
import com.example.ui.theme.*

data class ForecastDataPoint(
    val day: Int,
    val dateLabel: String,
    val balanceUsd: Double,
    val projectedIncomeUsd: Double,
    val projectedExpenseUsd: Double
)

@Composable
fun CashFlowAreaChart(
    currency: Currency,
    modifier: Modifier = Modifier
) {
    // Generate 30 days forecast trajectory
    val dataPoints = remember {
        val list = mutableListOf<ForecastDataPoint>()
        var running = 14200.0
        for (i in 1..30) {
            val income = if (i == 1 || i == 15 || i == 30) 3925.0 else 0.0
            val expense = if (i == 2 || i == 5 || i == 18) 450.0 else (80.0 + (i % 5) * 25.0)
            running += (income - expense)
            list.add(
                ForecastDataPoint(
                    day = i,
                    dateLabel = "Day $i",
                    balanceUsd = running,
                    projectedIncomeUsd = income,
                    projectedExpenseUsd = expense
                )
            )
        }
        list
    }

    var selectedIndex by remember { mutableIntStateOf(dataPoints.size - 1) }
    val currentPoint = dataPoints[selectedIndex.coerceIn(0, dataPoints.size - 1)]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cash_flow_forecast_chart")
    ) {
        // Scrubber Header Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                NeonBadge(text = "30-DAY FORECAST", color = NeonCyan)
                Text(
                    text = "Forecast Day ${currentPoint.day} / 30",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currency.format(currentPoint.balanceUsd),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Projected Balance",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
        }

        // Custom Area Canvas with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCommand)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val touchX = change.position.x
                        val width = size.width
                        val fraction = (touchX / width).coerceIn(0f, 1f)
                        selectedIndex = (fraction * (dataPoints.size - 1)).toInt()
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        selectedIndex = (fraction * (dataPoints.size - 1)).toInt()
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
                val w = size.width
                val h = size.height

                val minVal = dataPoints.minOf { it.balanceUsd } * 0.95
                val maxVal = dataPoints.maxOf { it.balanceUsd } * 1.05
                val range = (maxVal - minVal).coerceAtLeast(1.0)

                // Grid horizontal lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = h * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Build Path for curve
                val path = Path()
                val fillPath = Path()

                dataPoints.forEachIndexed { index, point ->
                    val x = (index.toFloat() / (dataPoints.size - 1)) * w
                    val y = h - ((point.balanceUsd - minVal) / range).toFloat() * h

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevIndex = index - 1
                        val prevX = (prevIndex.toFloat() / (dataPoints.size - 1)) * w
                        val prevY = h - ((dataPoints[prevIndex].balanceUsd - minVal) / range).toFloat() * h

                        val cx1 = prevX + (x - prevX) / 2
                        val cy1 = prevY
                        val cx2 = prevX + (x - prevX) / 2
                        val cy2 = y

                        path.cubicTo(cx1, cy1, cx2, cy2, x, y)
                        fillPath.cubicTo(cx1, cy1, cx2, cy2, x, y)
                    }
                }

                fillPath.lineTo(w, h)
                fillPath.close()

                // Draw Gradient Fill
                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = h
                )
                drawPath(fillPath, brush = gradientBrush, style = Fill)

                // Draw Neon Line
                drawPath(
                    path = path,
                    color = NeonCyan,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Selected Point Scrubber Line & Dot
                val selX = (selectedIndex.toFloat() / (dataPoints.size - 1)) * w
                val selPoint = dataPoints[selectedIndex]
                val selY = h - ((selPoint.balanceUsd - minVal) / range).toFloat() * h

                drawLine(
                    color = NeonCyan.copy(alpha = 0.6f),
                    start = Offset(selX, 0f),
                    end = Offset(selX, h),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Outer glowing circle
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(selX, selY)
                )
                // Inner bright circle
                drawCircle(
                    color = NeonCyan,
                    radius = 4.dp.toPx(),
                    center = Offset(selX, selY)
                )
            }
        }

        // Legend tags
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonCyan))
                    Text(text = "Projected Balance", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonEmerald))
                    Text(text = "Income Cash Inflow", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                }
            }
            Text(
                text = "Drag chart to inspect",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
            )
        }
    }
}

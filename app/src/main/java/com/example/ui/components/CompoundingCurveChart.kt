package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CompoundingYearData
import com.example.data.models.Currency
import com.example.ui.theme.*

@Composable
fun CompoundingCurveChart(
    projections: List<CompoundingYearData>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    if (projections.isEmpty()) return

    val maxVal = projections.maxOf { it.totalPortfolioValue } * 1.05
    val lastPoint = projections.last()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compounding_curve_chart")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "10-Year Terminal Wealth Target",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
                Text(
                    text = currency.format(lastPoint.totalPortfolioValue),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = NeonEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${currency.format(lastPoint.totalInterestCompounded)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Pure Compounding Alpha",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextCyan)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCommand)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 14.dp)) {
                val w = size.width
                val h = size.height

                // Horizontal Grid lines
                for (i in 0..4) {
                    val y = h * (i.toFloat() / 4)
                    drawLine(
                        color = Color(0x14FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Path for Total Portfolio (Emerald)
                val totalPath = Path()
                val totalFill = Path()

                // Path for Principal Base (Blue/Muted)
                val principalPath = Path()
                val principalFill = Path()

                projections.forEachIndexed { index, data ->
                    val x = (index.toFloat() / (projections.size - 1)) * w
                    val yTotal = h - (data.totalPortfolioValue / maxVal).toFloat() * h
                    val yPrincipal = h - (data.totalPrincipal / maxVal).toFloat() * h

                    if (index == 0) {
                        totalPath.moveTo(x, yTotal)
                        totalFill.moveTo(x, h)
                        totalFill.lineTo(x, yTotal)

                        principalPath.moveTo(x, yPrincipal)
                        principalFill.moveTo(x, h)
                        principalFill.lineTo(x, yPrincipal)
                    } else {
                        val prevX = ((index - 1).toFloat() / (projections.size - 1)) * w
                        val prevYTotal = h - (projections[index - 1].totalPortfolioValue / maxVal).toFloat() * h
                        val prevYPrincipal = h - (projections[index - 1].totalPrincipal / maxVal).toFloat() * h

                        val cx1 = prevX + (x - prevX) / 2
                        val cx2 = prevX + (x - prevX) / 2

                        totalPath.cubicTo(cx1, prevYTotal, cx2, yTotal, x, yTotal)
                        totalFill.cubicTo(cx1, prevYTotal, cx2, yTotal, x, yTotal)

                        principalPath.cubicTo(cx1, prevYPrincipal, cx2, yPrincipal, x, yPrincipal)
                        principalFill.cubicTo(cx1, prevYPrincipal, cx2, yPrincipal, x, yPrincipal)
                    }
                }

                totalFill.lineTo(w, h)
                totalFill.close()

                principalFill.lineTo(w, h)
                principalFill.close()

                // Draw Gradient Fills
                val totalGradient = Brush.verticalGradient(
                    listOf(NeonEmerald.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = h
                )
                val principalGradient = Brush.verticalGradient(
                    listOf(NeonBlue.copy(alpha = 0.2f), Color.Transparent),
                    startY = 0f,
                    endY = h
                )

                drawPath(totalFill, brush = totalGradient, style = Fill)
                drawPath(principalFill, brush = principalGradient, style = Fill)

                // Draw Strokes
                drawPath(principalPath, color = NeonBlue.copy(alpha = 0.6f), style = Stroke(width = 2.dp.toPx()))
                drawPath(totalPath, color = NeonEmerald, style = Stroke(width = 3.dp.toPx()))

                // Year Milestone Dots
                projections.filter { it.year == 0 || it.year == 3 || it.year == 5 || it.year == 7 || it.year == 10 }.forEach { data ->
                    val x = (data.year.toFloat() / (projections.size - 1)) * w
                    val y = h - (data.totalPortfolioValue / maxVal).toFloat() * h

                    drawCircle(color = NeonEmerald.copy(alpha = 0.4f), radius = 6.dp.toPx(), center = Offset(x, y))
                    drawCircle(color = NeonEmerald, radius = 3.dp.toPx(), center = Offset(x, y))
                }
            }
        }

        // Bottom labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonEmerald))
                    Text(text = "Compounded Balance", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(NeonBlue))
                    Text(text = "Principal Contributed", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                }
            }
            Text(text = "Year 0 ➔ Year 10", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
        }
    }
}

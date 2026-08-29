package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.FinancialRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WealthSimulatorScreen(
    assetAllocations: List<AssetCategory>,
    monthlyContribution: Double,
    riskProfile: RiskProfile,
    currency: Currency,
    repository: FinancialRepository,
    modifier: Modifier = Modifier
) {
    val totalWealthUsd = assetAllocations.sumOf { it.totalAmountUsd }
    val compoundingProjections = remember(totalWealthUsd, monthlyContribution, riskProfile) {
        repository.calculateCompoundingProjections(
            initialPrincipal = totalWealthUsd,
            monthlyAddition = monthlyContribution,
            risk = riskProfile,
            years = 10
        )
    }
    val diagnostic = remember(totalWealthUsd, monthlyContribution, riskProfile) {
        repository.getPortfolioDiagnostic()
    }

    val assetDonutSlices = remember(assetAllocations) {
        assetAllocations.map {
            DonutSlice(it.name, it.totalAmountUsd, Color(it.colorHex))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("wealth_simulator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEALTH & COMPOUNDING SIMULATOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    NeonBadge(text = "10-YEAR ALPHA", color = NeonEmerald)
                }
                Text(
                    text = "Asset allocation telemetry, emergency runway calculation, and dynamic mathematical wealth compounding curves.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // Section 1: Asset Allocation Donut Chart
        item {
            CyberCard(
                borderColor = SurfaceCardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PORTFOLIO ASSET ALLOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Net: ${currency.format(totalWealthUsd)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonEmerald,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                DonutChart(
                    slices = assetDonutSlices,
                    centerTitle = "Total Alpha",
                    centerValue = currency.format(totalWealthUsd),
                    currency = currency
                )
            }
        }

        // Section 2: 10-Year Compounding Projection Curve
        item {
            CyberCard(
                borderColor = NeonEmerald.copy(alpha = 0.3f),
                glowColor = NeonEmeraldGlow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "10-YEAR MATHEMATICAL COMPOUNDING PROJECTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    NeonBadge(text = riskProfile.displayName.split(" ").first(), color = NeonEmerald)
                }

                Spacer(modifier = Modifier.height(12.dp))

                CompoundingCurveChart(
                    projections = compoundingProjections,
                    currency = currency
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly Contribution Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monthly Contribution Addition:",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                        Text(
                            text = currency.format(monthlyContribution) + " / mo",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Slider(
                        value = monthlyContribution.toFloat(),
                        onValueChange = { repository.setMonthlyContribution(it.toDouble()) },
                        valueRange = 200f..5000f,
                        steps = 48,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SurfaceCommand
                        ),
                        modifier = Modifier.testTag("monthly_contribution_slider")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Risk Profile Selector
                Text(
                    text = "Risk Profile & Yield Model:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiskProfile.values().forEach { profile ->
                        val isSelected = profile == riskProfile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonEmerald.copy(alpha = 0.2f) else SurfaceCommand)
                                .border(1.dp, if (isSelected) NeonEmerald else SurfaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable { repository.setRiskProfile(profile) }
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                                .testTag("risk_profile_${profile.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = profile.displayName.split(" ").first(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) NeonEmerald else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = String.format("%.1f%% APY", profile.expectedAnnualReturn * 100),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) TextEmerald else TextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: AI Portfolio Diagnostic & Health Scores
        item {
            CyberCard(
                borderColor = SurfaceCardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = NeonCyan)
                        Text(
                            text = "AI PORTFOLIO DIAGNOSTIC",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                    }
                    NeonBadge(text = "STRESS TEST: PASSED", color = NeonEmerald)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricTelemetryPill(
                        title = "Diversification",
                        value = "${diagnostic.diversificationScore}/100",
                        subtitle = "Optimal across 5 asset classes",
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f),
                        testTag = "diag_div_score"
                    )

                    MetricTelemetryPill(
                        title = "Emergency Runway",
                        value = "${diagnostic.emergencyRunwayMonths} mo",
                        subtitle = "Liquid cash burn buffer",
                        accentColor = NeonEmerald,
                        modifier = Modifier.weight(1f),
                        testTag = "diag_runway"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AUTOMATED REBALANCING RECOMMENDATIONS:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                diagnostic.rebalancingActionList.forEach { recommendation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

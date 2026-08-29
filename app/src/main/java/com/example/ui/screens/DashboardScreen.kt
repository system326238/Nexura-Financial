package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Currency
import com.example.data.models.ScannedBill
import com.example.data.models.SettlementRail
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    currency: Currency,
    bills: List<ScannedBill>,
    onNavigateToTab: (String) -> Unit,
    onSettleBill: (String, SettlementRail) -> Unit,
    onSyncGoogleTasks: (ScannedBill) -> Unit,
    syncStatus: String?,
    modifier: Modifier = Modifier
) {
    var settlingBillId by remember { mutableStateOf<String?>(null) }
    val pendingBills = bills.filter { !it.isPaid }

    // Aggregate metrics
    val monthlyIncomeUsd = 7850.0
    val monthlyExpensesUsd = 4210.0
    val netCashFlowUsd = monthlyIncomeUsd - monthlyExpensesUsd
    val netTargetPacingFraction = (netCashFlowUsd / 4000.0).toFloat().coerceIn(0f, 1f)
    val safeToSpendDailyUsd = 94.20
    val hiddenFeesDetectedUsd = 142.80
    val totalInvestedWealthUsd = 148650.0

    // Spending breakdown slices for Donut Chart
    val spendingSlices = remember {
        listOf(
            DonutSlice("Housing", 1850.0, NeonCyan),
            DonutSlice("Food & Dining", 628.4, NeonEmerald),
            DonutSlice("Utilities", 312.8, NeonAmber),
            DonutSlice("Transport", 248.5, NeonViolet),
            DonutSlice("Tech & Subscriptions", 265.0, NeonRose)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("dashboard_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Top Forensic Warning Ribbon
        item {
            ForensicWarningBanner(
                title = "Forensic Alert",
                description = "Telecom 'Regulatory Recovery' & Utility riders detected. $142.80/mo projected waste.",
                actionText = "AUDIT",
                onActionClick = { onNavigateToTab("scanner") },
                modifier = Modifier.testTag("dashboard_forensic_banner")
            )
        }

        // Sleek Daily Safe-to-Spend Hero Card
        item {
            CyberCard(
                borderColor = SurfaceCardBorderGlow,
                glowColor = NeonCyanGlow,
                showAmbientGlow = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY SAFE-TO-SPEND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "+12.4% vs Avg",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Large Sleek Display Number
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = currency.symbol,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Light,
                            fontSize = 24.sp
                        )
                    )
                    val safeAmountStr = String.format("%.2f", safeToSpendDailyUsd * currency.rateAgainstUsd)
                    val parts = safeAmountStr.split(".")
                    Text(
                        text = parts[0],
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Light,
                            fontSize = 42.sp,
                            letterSpacing = (-1).sp
                        )
                    )
                    if (parts.size > 1) {
                        Text(
                            text = ".${parts[1]}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Light,
                                fontSize = 22.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sleek Gradient Progress Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x14FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.65f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SleekVelocityGradient)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SPENT ${currency.format(515.50)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "BUDGET ${currency.format(800.00)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // Deploy AI Auditor Quick Action Banner
        item {
            Surface(
                onClick = { onNavigateToTab("scanner") },
                shape = RoundedCornerShape(20.dp),
                color = NeonCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deploy_ai_auditor_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "DEPLOY AI AUDITOR",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Text(
                        text = "Forensic Mode Active",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Black.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Real-Time Financial Velocity Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FINANCIAL VELOCITY TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    NeonBadge(text = "LIVE FEED", color = NeonEmerald, pulse = true)
                }

                // Primary Net Cash Flow Velocity Card with Pacing Bar
                CyberCard(
                    borderColor = SurfaceCardBorder,
                    glowColor = null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NET MONTHLY CASH FLOW VELOCITY",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                            )
                            Text(
                                text = "+${currency.format(netCashFlowUsd)}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = NeonEmerald,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+24.8% PACING",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pacing Target Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pacing to ${currency.format(4000.0)} Target",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = String.format("%.0f%%", netTargetPacingFraction * 100),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0x14FFFFFF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(netTargetPacingFraction)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SleekVelocityGradient)
                            )
                        }
                    }
                }

                // Sub Metrics Grid (Exposed Fees, Total Invested)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricTelemetryPill(
                        title = "Exposed Fees",
                        value = currency.format(hiddenFeesDetectedUsd),
                        subtitle = "Detected creep / mo",
                        accentColor = NeonAmber,
                        icon = Icons.Default.Policy,
                        modifier = Modifier.weight(1f),
                        testTag = "exposed_fees_pill"
                    )

                    MetricTelemetryPill(
                        title = "Net Assets",
                        value = currency.format(totalInvestedWealthUsd),
                        subtitle = "+2.45% MTD",
                        accentColor = NeonEmerald,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f),
                        testTag = "total_wealth_pill"
                    )
                }
            }
        }

        // 30-Day Cash Flow Forecast Area Chart
        item {
            CyberCard(
                borderColor = SurfaceCardBorder
            ) {
                CashFlowAreaChart(currency = currency)
            }
        }

        // Category Spending Donut Chart
        item {
            CyberCard(
                borderColor = SurfaceCardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONTHLY EXPENSE DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Total: ${currency.format(monthlyExpensesUsd)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                DonutChart(
                    slices = spendingSlices,
                    centerTitle = "Total Burn",
                    centerValue = currency.format(monthlyExpensesUsd),
                    currency = currency
                )
            }
        }

        // Imminent Bills & Settlements Quick-Pay Rail
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IMMINENT INVOICE SETTLEMENTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (pendingBills.isNotEmpty()) {
                            NeonBadge(text = "${pendingBills.size} DUE", color = NeonAmber)
                        }
                    }

                    TextButton(
                        onClick = { onNavigateToTab("scanner") },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "View All Bills ➔",
                            style = MaterialTheme.typography.labelMedium.copy(color = NeonCyan)
                        )
                    }
                }

                if (syncStatus != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = syncStatus,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextCyan,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                if (pendingBills.isEmpty()) {
                    CyberCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald)
                            Text(
                                text = "All imminent invoices settled. Zero pending carrier liabilities.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    }
                } else {
                    pendingBills.take(2).forEach { bill ->
                        BillQuickPayCard(
                            bill = bill,
                            currency = currency,
                            onSettleClick = { settlingBillId = bill.id },
                            onSyncGoogleTasks = { onSyncGoogleTasks(bill) }
                        )
                    }
                }
            }
        }
    }

    // Settlement Rails Modal
    if (settlingBillId != null) {
        val targetBill = bills.find { it.id == settlingBillId }
        if (targetBill != null) {
            SettlementModal(
                bill = targetBill,
                currency = currency,
                onDismiss = { settlingBillId = null },
                onConfirmSettle = { rail ->
                    onSettleBill(targetBill.id, rail)
                    settlingBillId = null
                }
            )
        }
    }
}

@Composable
fun BillQuickPayCard(
    bill: ScannedBill,
    currency: Currency,
    onSettleClick: () -> Unit,
    onSyncGoogleTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.testTag("bill_card_${bill.id}"),
        borderColor = if (bill.hiddenFees.isNotEmpty()) NeonCrimson.copy(alpha = 0.4f) else SurfaceCardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = bill.providerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Due: ${bill.dueDate} • ${bill.category}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 11.sp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currency.format(bill.totalAmountDue),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (bill.hiddenFees.isNotEmpty()) {
                    Text(
                        text = "⚠️ ${currency.format(bill.hiddenFees.sumOf { it.amount })} phantom fee",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonCrimson, fontSize = 10.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSyncGoogleTasks,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).testTag("sync_google_tasks_${bill.id}")
            ) {
                Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Google Tasks", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
            }

            Button(
                onClick = onSettleClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CanvasBlack),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).testTag("settle_button_${bill.id}")
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Instant Settle",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SettlementModal(
    bill: ScannedBill,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirmSettle: (SettlementRail) -> Unit
) {
    var selectedRail by remember { mutableStateOf(SettlementRail.FEDNOW) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                NeonBadge(text = "ZERO-FEE DIRECT SETTLEMENT RAILS", color = NeonCyan)
                Text(
                    text = "Settle ${bill.providerName}",
                    style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Amount: ${currency.format(bill.totalAmountDue)}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = NeonEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select high-speed settlement network:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                SettlementRail.values().forEach { rail ->
                    val isSelected = rail == selectedRail
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.12f) else SurfaceCommand)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else SurfaceCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedRail = rail }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = rail.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isSelected) NeonCyan else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "${rail.speed} • ${rail.feeDescription}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedRail = rail },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSettle(selectedRail) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = CanvasBlack),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_settlement_button")
            ) {
                Text(
                    text = "Execute Settlement",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
            }
        }
    )
}

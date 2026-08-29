package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiService
import com.example.data.models.BenchmarkRating
import com.example.data.models.Currency
import com.example.data.models.Subscription
import com.example.ui.components.CyberCard
import com.example.ui.components.NeonBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TruthCostAuditorScreen(
    subscriptions: List<Subscription>,
    currency: Currency,
    onToggleSubscription: (String) -> Unit,
    geminiService: GeminiService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Truth Cost Calculator State
    var statedPriceInput by remember { mutableStateOf("89.99") }
    var hiddenFeesInput by remember { mutableStateOf("14.50") }
    var equipmentFeeInput by remember { mutableStateOf("18.00") }
    var inflationRateInput by remember { mutableStateOf("4.5") }
    var providerNameInput by remember { mutableStateOf("Apex Broadband") }

    val statedPrice = statedPriceInput.toDoubleOrNull() ?: 0.0
    val hiddenFee = hiddenFeesInput.toDoubleOrNull() ?: 0.0
    val equipmentFee = equipmentFeeInput.toDoubleOrNull() ?: 0.0
    val inflationRate = (inflationRateInput.toDoubleOrNull() ?: 4.5) / 100.0

    val actualMonthlyCost = statedPrice + hiddenFee + equipmentFee
    val truthAnnualCost = actualMonthlyCost * 12 * (1 + inflationRate)
    val truthThreeYearCost = (actualMonthlyCost * 12) + (actualMonthlyCost * 12 * (1 + inflationRate)) + (actualMonthlyCost * 12 * (1 + inflationRate) * (1 + inflationRate))
    val totalHiddenLeakAnnual = (hiddenFee + equipmentFee) * 12

    // Benchmark rating
    val marketBenchmarkUsd = 70.00
    val markupPercentage = if (marketBenchmarkUsd > 0) ((actualMonthlyCost - marketBenchmarkUsd) / marketBenchmarkUsd) * 100 else 0.0
    val benchmarkRating = when {
        markupPercentage > 40 -> BenchmarkRating.PREDATORY
        markupPercentage > 15 -> BenchmarkRating.ELEVATED
        else -> BenchmarkRating.FAIR
    }

    // AI Negotiation Script Generator State
    var generatedScript by remember { mutableStateOf<String?>(null) }
    var isGeneratingScript by remember { mutableStateOf(false) }

    fun generateNegotiationScript() {
        isGeneratingScript = true
        scope.launch {
            val script = geminiService.generateNegotiationScript(
                providerName = providerNameInput,
                feeName = "Regulatory Cost Recovery & Modem Fee",
                feeAmount = hiddenFee + equipmentFee,
                scenarioContext = "Billed for customer-owned gateway and non-governmental regulatory markup on $statedPriceInput/mo broadband plan."
            )
            generatedScript = script
            isGeneratingScript = false
        }
    }

    // Overlap calculations
    val streamingSubs = subscriptions.filter { it.category == "Streaming Video" }
    val cloudSubs = subscriptions.filter { it.category == "Cloud Storage" }
    val totalAnnualSubscriptionWaste = subscriptions.filter { it.usageFrequency.contains("Low") && it.active }.sumOf { it.truthAnnualCost }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("truth_cost_auditor_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        // Top Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRUTH COST & OVERLAP AUDITOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    NeonBadge(text = "ANTI-JUNK FEE CORE", color = NeonCrimson)
                }
                Text(
                    text = "Deconstructs deceptive monthly quotes into 1-year and 3-year true cash commitments. Pinpoints overlapping subscription drain.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // Section 1: Forensic Truth Cost Calculator
        item {
            CyberCard(
                borderColor = NeonCyan.copy(alpha = 0.3f),
                glowColor = NeonCyanGlow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FORENSIC TRUTH COST CALCULATOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    NeonBadge(text = "DE-CREEP ENGINE", color = NeonCyan)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input fields
                OutlinedTextField(
                    value = providerNameInput,
                    onValueChange = { providerNameInput = it },
                    label = { Text("Provider / Service Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("provider_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = statedPriceInput,
                        onValueChange = { statedPriceInput = it },
                        label = { Text("Stated Monthly ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("stated_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = hiddenFeesInput,
                        onValueChange = { hiddenFeesInput = it },
                        label = { Text("Phantom Fees ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("hidden_fees_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCrimson,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = equipmentFeeInput,
                        onValueChange = { equipmentFeeInput = it },
                        label = { Text("Equipment/Hardware ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("equipment_fee_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonAmber,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = inflationRateInput,
                        onValueChange = { inflationRateInput = it },
                        label = { Text("Annual Creep (%)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("inflation_rate_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Results Matrix
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCommand)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Actual Monthly Out-of-Pocket:", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text(text = currency.format(actualMonthlyCost), style = MaterialTheme.typography.titleMedium.copy(color = NeonCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "1-Year Truth Cost (with Creep):", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text(text = currency.format(truthAnnualCost), style = MaterialTheme.typography.titleMedium.copy(color = NeonAmber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "3-Year Contract Truth Cost:", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text(text = currency.format(truthThreeYearCost), style = MaterialTheme.typography.titleLarge.copy(color = NeonCrimson, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                        }
                        Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Stealth Fee Drain (Annual):", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                            Text(text = "-${currency.format(totalHiddenLeakAnnual)} / yr", style = MaterialTheme.typography.labelMedium.copy(color = NeonCrimson, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Regulatory Benchmark Rating
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (benchmarkRating) {
                                BenchmarkRating.PREDATORY -> Color(0xFF2E0C12)
                                BenchmarkRating.ELEVATED -> Color(0xFF2E1C0C)
                                BenchmarkRating.FAIR -> Color(0xFF0C2E1A)
                            }
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REGULATORY BENCHMARK SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                        Text(
                            text = when (benchmarkRating) {
                                BenchmarkRating.PREDATORY -> "PREDATORY (+${String.format("%.0f", markupPercentage)}% above market avg)"
                                BenchmarkRating.ELEVATED -> "ELEVATED (+${String.format("%.0f", markupPercentage)}% above market avg)"
                                BenchmarkRating.FAIR -> "FAIR TARIFF (Within normal regional variance)"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = when (benchmarkRating) {
                                    BenchmarkRating.PREDATORY -> NeonCrimson
                                    BenchmarkRating.ELEVATED -> NeonAmber
                                    BenchmarkRating.FAIR -> NeonEmerald
                                },
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Section 2: AI Negotiation Script Generator
        item {
            CyberCard(
                borderColor = NeonViolet.copy(alpha = 0.4f),
                glowColor = NeonVioletGlow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = NeonViolet)
                        Text(
                            text = "AI RETENTION SCRIPT GENERATOR",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                    }
                    NeonBadge(text = "GEMINI 3.5", color = NeonViolet)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Generate custom retention waiver demands citing FCC Truth-in-Billing and FTC Negative Option regulations.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { generateNegotiationScript() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("generate_script_button"),
                    enabled = !isGeneratingScript
                ) {
                    if (isGeneratingScript) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Synthesizing Legal Waiver Script...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Generate Negotiation Script for $providerNameInput", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                if (generatedScript != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCommand)
                            .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeonBadge(text = "TACTICAL SCRIPT READY", color = NeonEmerald)
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Negotiation Script", generatedScript)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("copy_script_button")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = NeonCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Copy Script", style = MaterialTheme.typography.labelSmall.copy(color = TextCyan, fontSize = 10.sp))
                                }
                            }

                            Text(
                                text = generatedScript!!,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Subscription Overlap Detector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SUBSCRIPTION OVERLAP DETECTOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Estimated Waste: ${currency.format(totalAnnualSubscriptionWaste)} / year",
                            style = MaterialTheme.typography.bodyMedium.copy(color = NeonCrimson, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    NeonBadge(text = "${subscriptions.count { it.active }} ACTIVE", color = NeonCyan)
                }

                subscriptions.forEach { sub ->
                    SubscriptionItemCard(
                        subscription = sub,
                        currency = currency,
                        onToggleActive = { onToggleSubscription(sub.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionItemCard(
    subscription: Subscription,
    currency: Currency,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier.testTag("subscription_card_${subscription.id}"),
        borderColor = if (subscription.usageFrequency.contains("Low") && subscription.active) NeonCrimson.copy(alpha = 0.4f) else SurfaceCardBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (subscription.active) TextPrimary else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (subscription.usageFrequency.contains("Low")) {
                        NeonBadge(text = "OVERLAP DRAIN", color = NeonCrimson, fontSize = 9)
                    }
                }
                Text(
                    text = "${subscription.category} • Usage: ${subscription.usageFrequency}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 11.sp)
                )
                Text(
                    text = "Truth Cost: ${currency.format(subscription.truthAnnualCost)} / yr (Stated: ${currency.format(subscription.statedMonthlyPrice)}/mo)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (subscription.active) NeonCyan else TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Switch(
                checked = subscription.active,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = SurfaceCommand
                ),
                modifier = Modifier.testTag("toggle_sub_${subscription.id}")
            )
        }
    }
}

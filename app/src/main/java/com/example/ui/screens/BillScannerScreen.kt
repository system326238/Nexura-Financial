package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiService
import com.example.data.models.Currency
import com.example.data.models.FeeSeverity
import com.example.data.models.ScannedBill
import com.example.data.models.SettlementRail
import com.example.ui.components.CyberCard
import com.example.ui.components.LaserScanOverlay
import com.example.ui.components.NeonBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BillScannerScreen(
    bills: List<ScannedBill>,
    currency: Currency,
    onSettleBill: (String, SettlementRail) -> Unit,
    onSyncGoogleTasks: (ScannedBill) -> Unit,
    geminiService: GeminiService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBillIndex by remember { mutableIntStateOf(0) }
    val currentBill = bills.getOrNull(selectedBillIndex) ?: bills.firstOrNull()

    var isScanning by remember { mutableStateOf(false) }
    var aiForensicAnalysisResult by remember { mutableStateOf<String?>(null) }
    var isGeneratingAiAudit by remember { mutableStateOf(false) }
    var settlingBillId by remember { mutableStateOf<String?>(null) }

    fun triggerScan(index: Int) {
        selectedBillIndex = index
        isScanning = true
        aiForensicAnalysisResult = null
        scope.launch {
            delay(1500) // Holographic scan effect
            isScanning = false
        }
    }

    fun requestDeepAiAudit() {
        if (currentBill == null) return
        isGeneratingAiAudit = true
        scope.launch {
            val result = geminiService.analyzeBillForensics(
                billText = "${currentBill.providerName} Invoice #${currentBill.invoiceNumber}. Total: $${currentBill.totalAmountDue}. Items: ${currentBill.lineItems.joinToString { it.description + ": $" + it.amount }}",
                billCategory = currentBill.category
            )
            aiForensicAnalysisResult = result
            isGeneratingAiAudit = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("bill_scanner_screen"),
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
                        text = "AI FORENSIC BILL SCANNER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    NeonBadge(text = "HOLOGRAPHIC OCR", color = NeonCyan)
                }
                Text(
                    text = "Multimodal deep-packet invoice analyzer. Exposes disguised fees, unbundled codes, and auto-renew creep.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }

        // Scenario Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Ingestion Scenario or Test Bill:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bills.size) { idx ->
                        val bill = bills[idx]
                        val isSelected = idx == selectedBillIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else SurfaceCommand)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else SurfaceCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { triggerScan(idx) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("scenario_chip_$idx")
                        ) {
                            Column {
                                Text(
                                    text = bill.providerName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSelected) NeonCyan else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "${bill.category} • ${currency.format(bill.totalAmountDue)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) TextCyan else TextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ingestion Trigger Actions (Camera, Upload, Rescan)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { triggerScan(selectedBillIndex) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f).testTag("camera_scan_button")
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Camera Capture", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { triggerScan(selectedBillIndex) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f).testTag("file_upload_button")
                ) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Upload Invoice", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Holographic Laser Scanning Overlay
        if (isScanning) {
            item {
                LaserScanOverlay(isScanning = true)
            }
        }

        if (currentBill != null && !isScanning) {
            // Main Forensic Bill Summary Card
            item {
                CyberCard(
                    borderColor = if (currentBill.hiddenFees.isNotEmpty()) NeonCrimson.copy(alpha = 0.5f) else SurfaceCardBorder,
                    glowColor = if (currentBill.hiddenFees.isNotEmpty()) Color(0x33EF4444) else null
                ) {
                    // Provider header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentBill.providerName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (currentBill.isPaid) {
                                    NeonBadge(text = "SETTLED", color = NeonEmerald)
                                }
                            }
                            Text(
                                text = "Invoice #${currentBill.invoiceNumber} • Period: ${currentBill.billingPeriod}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 11.sp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currency.format(currentBill.totalAmountDue),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = if (currentBill.isPaid) NeonEmerald else TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            )
                            Text(
                                text = "Due by ${currentBill.dueDate}",
                                style = MaterialTheme.typography.labelSmall.copy(color = NeonAmber)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Forensic Alert Findings
                    if (currentBill.hiddenFees.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF240A10))
                                .border(1.dp, NeonCrimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = NeonCrimson,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${currentBill.hiddenFees.size} STEALTH SURCHARGES DETECTED",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = NeonCrimson,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = currentBill.forensicSummary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line Items Forensic Breakdown
                    Text(
                        text = "ITEMIZED FORENSIC AUDIT LEDGER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    currentBill.lineItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isFlagged) Icons.Default.Cancel else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (item.isFlagged) NeonCrimson else NeonEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Column {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (item.isFlagged) NeonCrimson else TextPrimary,
                                            fontSize = 12.sp
                                        )
                                    )
                                    if (item.isFlagged && item.flagReason != null) {
                                        Text(
                                            text = item.flagReason,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NeonAmber,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = currency.format(item.amount),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (item.isFlagged) NeonCrimson else TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Divider(color = Color(0x14FFFFFF), thickness = 0.5.dp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtotal, Taxes, Total
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Subtotal", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                        Text(text = currency.format(currentBill.subtotal), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontFamily = FontFamily.Monospace))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Statutory Taxes", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                        Text(text = currency.format(currentBill.taxes), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontFamily = FontFamily.Monospace))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Amount Due", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        Text(text = currency.format(currentBill.totalAmountDue), style = MaterialTheme.typography.titleMedium.copy(color = NeonEmerald, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons (Settle, Google Tasks, AI Deep Audit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSyncGoogleTasks(currentBill) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f).testTag("bill_detail_sync_tasks")
                        ) {
                            Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Sync Tasks", style = MaterialTheme.typography.labelSmall)
                        }

                        if (!currentBill.isPaid) {
                            Button(
                                onClick = { settlingBillId = currentBill.id },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = CanvasBlack),
                                modifier = Modifier.weight(1f).testTag("bill_detail_settle_button")
                            ) {
                                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Settle Bill", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { requestDeepAiAudit() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("deep_ai_audit_button"),
                        enabled = !isGeneratingAiAudit
                    ) {
                        if (isGeneratingAiAudit) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Running Gemini 3.1 Pro Audit...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Run Gemini 3.1 Pro Deep Audit", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Digital Receipt Card if Paid
            if (currentBill.isPaid && currentBill.paymentReceipt != null) {
                item {
                    val receipt = currentBill.paymentReceipt
                    CyberCard(
                        borderColor = NeonEmerald.copy(alpha = 0.5f),
                        glowColor = NeonEmeraldGlow
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NeonBadge(text = "SETTLEMENT RECEIPT", color = NeonEmerald)
                            Text(
                                text = receipt.timestamp,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "TxID: ${receipt.transactionId}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Network: ${receipt.settlementRail.title} • Status: ${receipt.status}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 12.sp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("TxID", receipt.transactionId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Transaction ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Copy TxID Hash", style = MaterialTheme.typography.labelSmall.copy(color = TextCyan, fontSize = 10.sp))
                        }
                    }
                }
            }

            // Gemini Deep Audit Result Card
            if (aiForensicAnalysisResult != null) {
                item {
                    CyberCard(
                        borderColor = NeonViolet.copy(alpha = 0.5f),
                        glowColor = NeonVioletGlow
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = NeonViolet)
                            Text(
                                text = "GEMINI 3.1 PRO FORENSIC DIAGNOSTIC",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = aiForensicAnalysisResult!!,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }

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

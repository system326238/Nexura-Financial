package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ai.GeminiService
import com.example.data.models.Currency
import com.example.data.repository.FinancialRepository
import com.example.security.SecurityRepository
import com.example.ui.components.CurrencySelectorDropdown
import com.example.ui.components.MobileDeviceFrame
import com.example.ui.components.NeonBadge
import com.example.ui.components.SecuritySettingsModal
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : FragmentActivity() {
    private val repository = FinancialRepository()
    private val geminiService = GeminiService()
    private lateinit var securityRepository: SecurityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        securityRepository = SecurityRepository(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isSetupCompleted by securityRepository.isSetupCompleted.collectAsState()
                val isUnlocked by securityRepository.isUnlocked.collectAsState()

                Crossfade(
                    targetState = when {
                        !isSetupCompleted -> "setup"
                        !isUnlocked -> "lock"
                        else -> "app"
                    },
                    label = "security_nav_transition"
                ) { screenState ->
                    when (screenState) {
                        "setup" -> {
                            SecuritySetupScreen(
                                securityRepository = securityRepository,
                                onSetupComplete = {
                                    // Setup completed and unlocked automatically
                                }
                            )
                        }
                        "lock" -> {
                            SecurityLockScreen(
                                securityRepository = securityRepository,
                                onUnlocked = {
                                    // Unlocked
                                }
                            )
                        }
                        else -> {
                            NexuraFinancialApp(
                                repository = repository,
                                geminiService = geminiService,
                                securityRepository = securityRepository
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Ensure biometric privacy by locking on background if configured
        securityRepository.lockApp()
    }
}

enum class NavigationTab(val id: String, val label: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Command", Icons.Default.Dashboard),
    SCANNER("scanner", "Scanner", Icons.Default.QrCodeScanner),
    AUDITOR("auditor", "Auditor", Icons.Default.Gavel),
    BUDGET("budget", "Envelopes", Icons.Default.AccountBalanceWallet),
    WEALTH("wealth", "Wealth", Icons.Default.TrendingUp),
    COPILOT("copilot", "Copilot", Icons.Default.Psychology)
}

@Composable
fun NexuraFinancialApp(
    repository: FinancialRepository,
    geminiService: GeminiService,
    securityRepository: SecurityRepository
) {
    var activeTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var isFrameEnabled by remember { mutableStateOf(false) }
    var showSecurityModal by remember { mutableStateOf(false) }

    val currency by repository.selectedCurrency.collectAsState()
    val bills by repository.scannedBills.collectAsState()
    val subscriptions by repository.subscriptions.collectAsState()
    val envelopes by repository.budgetEnvelopes.collectAsState()
    val transactions by repository.transactions.collectAsState()
    val assetAllocations by repository.assetAllocations.collectAsState()
    val monthlyContribution by repository.monthlyContribution.collectAsState()
    val riskProfile by repository.riskProfile.collectAsState()
    val activePersona by repository.activePersona.collectAsState()
    val chatMessages by repository.chatMessages.collectAsState()
    val googleSyncStatus by repository.googleSyncStatus.collectAsState()
    val sheetsExportStatus by repository.sheetsExportStatus.collectAsState()

    fun navigateToTabId(tabId: String) {
        val matched = NavigationTab.values().find { it.id == tabId }
        if (matched != null) {
            activeTab = matched
        }
    }

    MobileDeviceFrame(
        isFrameEnabled = isFrameEnabled,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBlack),
            containerColor = CanvasBlack,
            topBar = {
                NexuraTopAppBar(
                    selectedCurrency = currency,
                    onCurrencyChange = { repository.setCurrency(it) },
                    isFrameEnabled = isFrameEnabled,
                    onToggleFrame = { isFrameEnabled = !isFrameEnabled },
                    onOpenSecurity = { showSecurityModal = true },
                    onLockVault = { securityRepository.lockApp() }
                )
            },
            bottomBar = {
                NexuraBottomNavigation(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        currency = currency,
                        bills = bills,
                        onNavigateToTab = { navigateToTabId(it) },
                        onSettleBill = { id, rail -> repository.settleBill(id, rail) },
                        onSyncGoogleTasks = { repository.syncBillToGoogleTasks(it) },
                        syncStatus = googleSyncStatus
                    )
                    NavigationTab.SCANNER -> BillScannerScreen(
                        bills = bills,
                        currency = currency,
                        onSettleBill = { id, rail -> repository.settleBill(id, rail) },
                        onSyncGoogleTasks = { repository.syncBillToGoogleTasks(it) },
                        geminiService = geminiService
                    )
                    NavigationTab.AUDITOR -> TruthCostAuditorScreen(
                        subscriptions = subscriptions,
                        currency = currency,
                        onToggleSubscription = { repository.toggleSubscription(it) },
                        geminiService = geminiService
                    )
                    NavigationTab.BUDGET -> BudgetLedgerScreen(
                        envelopes = envelopes,
                        transactions = transactions,
                        currency = currency,
                        onAddTransaction = { repository.addTransaction(it) },
                        onExportGoogleSheets = { repository.exportToGoogleSheets() },
                        sheetsExportStatus = sheetsExportStatus
                    )
                    NavigationTab.WEALTH -> WealthSimulatorScreen(
                        assetAllocations = assetAllocations,
                        monthlyContribution = monthlyContribution,
                        riskProfile = riskProfile,
                        currency = currency,
                        repository = repository
                    )
                    NavigationTab.COPILOT -> CopilotScreen(
                        messages = chatMessages,
                        activePersona = activePersona,
                        currency = currency,
                        onSendMessage = { text ->
                            repository.addChatMessage(
                                text = text,
                                sender = com.example.data.models.MessageSender.USER,
                                persona = activePersona
                            )
                        },
                        onSelectPersona = { repository.setActivePersona(it) },
                        onNavigateToTab = { navigateToTabId(it) },
                        geminiService = geminiService
                    )
                }
            }
        }

        // Security Settings Modal Sheet
        if (showSecurityModal) {
            SecuritySettingsModal(
                securityRepository = securityRepository,
                onDismiss = { showSecurityModal = false },
                onLockVault = { securityRepository.lockApp() },
                onResetSetup = { securityRepository.resetSecurity() }
            )
        }
    }
}

@Composable
fun NexuraTopAppBar(
    selectedCurrency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    isFrameEnabled: Boolean,
    onToggleFrame: () -> Unit,
    onOpenSecurity: () -> Unit = {},
    onLockVault: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CanvasBlack)
            .border(1.dp, SurfaceCardBorder)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("nexura_top_app_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title with Gradient
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEXURA",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = SleekBrandGradient,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "FINANCIAL OS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            // Top Actions: Currency Selector, Status Orb & Frame Toggle
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurrencySelectorDropdown(
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = onCurrencyChange
                )

                // Sleek Status Avatar / Forensic Orb / Security Trigger
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
                        .clickable { onOpenSecurity() }
                        .testTag("security_status_orb_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Settings & Vault Status",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Quick Lock Vault Button
                IconButton(
                    onClick = onLockVault,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceCardBorder, CircleShape)
                        .testTag("quick_lock_vault_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Vault",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Android Native Frame Toggle
                IconButton(
                    onClick = onToggleFrame,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isFrameEnabled) NeonCyan.copy(alpha = 0.2f) else SurfaceElevated)
                        .border(1.dp, if (isFrameEnabled) NeonCyan else SurfaceCardBorder, CircleShape)
                        .testTag("android_frame_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Toggle Device Frame",
                        tint = if (isFrameEnabled) NeonCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NexuraBottomNavigation(
    activeTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = CanvasBlack,
        contentColor = TextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceCardBorder)
            .testTag("nexura_bottom_navigation")
    ) {
        NavigationTab.values().forEach { tab ->
            val isSelected = tab == activeTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(NeonCyan)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) NeonCyan else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) NeonCyan else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.testTag("nav_tab_${tab.id}")
            )
        }
    }
}

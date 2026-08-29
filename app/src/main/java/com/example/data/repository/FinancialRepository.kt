package com.example.data.repository

import com.example.data.models.AssetCategory
import com.example.data.models.BillLineItem
import com.example.data.models.BudgetEnvelope
import com.example.data.models.ChatMessage
import com.example.data.models.CompoundingYearData
import com.example.data.models.CopilotPersona
import com.example.data.models.Currency
import com.example.data.models.ExpenseCategory
import com.example.data.models.FeeSeverity
import com.example.data.models.HiddenFee
import com.example.data.models.MessageSender
import com.example.data.models.PaymentReceipt
import com.example.data.models.PortfolioDiagnostic
import com.example.data.models.RiskProfile
import com.example.data.models.ScannedBill
import com.example.data.models.SettlementRail
import com.example.data.models.Subscription
import com.example.data.models.Transaction
import com.example.data.models.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FinancialRepository {

    private val _selectedCurrency = MutableStateFlow(Currency.USD)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    private val _isDeviceFrameEnabled = MutableStateFlow(false)
    val isDeviceFrameEnabled: StateFlow<Boolean> = _isDeviceFrameEnabled.asStateFlow()

    private val _bills = MutableStateFlow<List<ScannedBill>>(emptyList())
    val bills: StateFlow<List<ScannedBill>> = _bills.asStateFlow()
    val scannedBills: StateFlow<List<ScannedBill>> = _bills.asStateFlow()

    private val _envelopes = MutableStateFlow<List<BudgetEnvelope>>(emptyList())
    val envelopes: StateFlow<List<BudgetEnvelope>> = _envelopes.asStateFlow()
    val budgetEnvelopes: StateFlow<List<BudgetEnvelope>> = _envelopes.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()

    private val _assetAllocations = MutableStateFlow<List<AssetCategory>>(emptyList())
    val assetAllocations: StateFlow<List<AssetCategory>> = _assetAllocations.asStateFlow()

    private val _currentMonthlyContribution = MutableStateFlow(1200.0)
    val currentMonthlyContribution: StateFlow<Double> = _currentMonthlyContribution.asStateFlow()
    val monthlyContribution: StateFlow<Double> = _currentMonthlyContribution.asStateFlow()

    private val _currentRiskProfile = MutableStateFlow(RiskProfile.MODERATE)
    val currentRiskProfile: StateFlow<RiskProfile> = _currentRiskProfile.asStateFlow()
    val riskProfile: StateFlow<RiskProfile> = _currentRiskProfile.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activePersona = MutableStateFlow(CopilotPersona.FORENSIC_AUDITOR)
    val activePersona: StateFlow<CopilotPersona> = _activePersona.asStateFlow()

    private val _googleTasksSyncStatus = MutableStateFlow<String?>(null)
    val googleTasksSyncStatus: StateFlow<String?> = _googleTasksSyncStatus.asStateFlow()
    val googleSyncStatus: StateFlow<String?> = _googleTasksSyncStatus.asStateFlow()

    private val _googleSheetsExportStatus = MutableStateFlow<String?>(null)
    val googleSheetsExportStatus: StateFlow<String?> = _googleSheetsExportStatus.asStateFlow()
    val sheetsExportStatus: StateFlow<String?> = _googleSheetsExportStatus.asStateFlow()

    init {
        seedInitialData()
    }

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun toggleDeviceFrame() {
        _isDeviceFrameEnabled.value = !_isDeviceFrameEnabled.value
    }

    fun setDeviceFrame(enabled: Boolean) {
        _isDeviceFrameEnabled.value = enabled
    }

    fun setMonthlyContribution(amount: Double) {
        _currentMonthlyContribution.value = amount
    }

    fun setRiskProfile(profile: RiskProfile) {
        _currentRiskProfile.value = profile
    }

    fun setPersona(persona: CopilotPersona) {
        _activePersona.value = persona
    }

    fun setActivePersona(persona: CopilotPersona) {
        _activePersona.value = persona
    }

    fun addChatMessage(message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
    }

    fun addChatMessage(text: String, sender: MessageSender, persona: CopilotPersona?) {
        val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date())
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = sender,
            text = text,
            timestamp = timeStr,
            persona = persona
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    fun addTransaction(transaction: Transaction) {
        val current = _transactions.value.toMutableList()
        current.add(0, transaction)
        _transactions.value = current

        // Update envelope if it is an expense
        if (transaction.type == TransactionType.EXPENSE) {
            val envList = _envelopes.value.map { env ->
                if (env.category == transaction.category) {
                    env.copy(spentCurrentMonth = env.spentCurrentMonth + transaction.amountUsd)
                } else env
            }
            _envelopes.value = envList
        }
    }

    fun settleBill(billId: String, rail: SettlementRail): PaymentReceipt {
        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US).format(Date())
        val txId = "NX-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val bill = _bills.value.find { it.id == billId }
        val receipt = PaymentReceipt(
            transactionId = txId,
            paidAmount = bill?.totalAmountDue ?: 0.0,
            currencyCode = _selectedCurrency.value.code,
            settlementRail = rail,
            timestamp = dateStr
        )

        _bills.value = _bills.value.map {
            if (it.id == billId) {
                it.copy(isPaid = true, paymentReceipt = receipt)
            } else it
        }

        // Add to transaction ledger
        if (bill != null) {
            addTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    title = "Bill Settlement: ${bill.providerName}",
                    merchant = bill.providerName,
                    category = when (bill.category) {
                        "Utility" -> ExpenseCategory.UTILITIES
                        "Telecom" -> ExpenseCategory.TECH
                        "Medical" -> ExpenseCategory.HEALTH
                        "SaaS" -> ExpenseCategory.TECH
                        else -> ExpenseCategory.UTILITIES
                    },
                    amountUsd = bill.totalAmountDue,
                    type = TransactionType.EXPENSE,
                    date = "Today",
                    note = "Settled via ${rail.title} (TxID: $txId)"
                )
            )
        }

        return receipt
    }

    fun toggleSubscription(id: String) {
        _subscriptions.value = _subscriptions.value.map {
            if (it.id == id) it.copy(active = !it.active) else it
        }
    }

    fun syncBillToGoogleTasks(bill: ScannedBill) {
        _googleTasksSyncStatus.value = "Synced '${bill.providerName}' due on ${bill.dueDate} to Google Tasks ✅"
    }

    fun clearTasksSyncStatus() {
        _googleTasksSyncStatus.value = null
    }

    fun exportToGoogleSheets(): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        val sheetName = "Nexura_Financial_Ledger_$timestamp.gsheet"
        _googleSheetsExportStatus.value = "Exported 3 tabs (CashFlow, Envelopes, Bills) with live SUM formulas to Google Sheets ($sheetName) 📊"
        return sheetName
    }

    fun clearSheetsExportStatus() {
        _googleSheetsExportStatus.value = null
    }

    fun calculateCompoundingProjections(
        initialPrincipal: Double = 35000.0,
        monthlyAddition: Double,
        risk: RiskProfile,
        years: Int = 10
    ): List<CompoundingYearData> {
        val annualRate = risk.expectedAnnualReturn
        val monthlyRate = annualRate / 12.0
        val results = mutableListOf<CompoundingYearData>()

        var runningBalance = initialPrincipal
        var totalPrincipalContributed = initialPrincipal

        results.add(
            CompoundingYearData(
                year = 0,
                totalPrincipal = initialPrincipal,
                totalInterestCompounded = 0.0,
                totalPortfolioValue = initialPrincipal
            )
        )

        for (y in 1..years) {
            for (m in 1..12) {
                runningBalance = (runningBalance + monthlyAddition) * (1 + monthlyRate)
                totalPrincipalContributed += monthlyAddition
            }
            val interest = runningBalance - totalPrincipalContributed
            results.add(
                CompoundingYearData(
                    year = y,
                    totalPrincipal = totalPrincipalContributed,
                    totalInterestCompounded = interest.coerceAtLeast(0.0),
                    totalPortfolioValue = runningBalance
                )
            )
        }
        return results
    }

    fun getPortfolioDiagnostic(): PortfolioDiagnostic {
        val totalAssets = _assetAllocations.value.sumOf { it.totalAmountUsd }
        val liquidCash = _assetAllocations.value.find { it.name.contains("Cash") }?.totalAmountUsd ?: 0.0
        val monthlyBurn = _envelopes.value.sumOf { it.allocatedMonthly }
        val runway = if (monthlyBurn > 0) liquidCash / monthlyBurn else 6.0
        val projections = calculateCompoundingProjections(
            initialPrincipal = totalAssets,
            monthlyAddition = _currentMonthlyContribution.value,
            risk = _currentRiskProfile.value,
            years = 10
        )

        return PortfolioDiagnostic(
            diversificationScore = 88,
            emergencyRunwayMonths = String.format(Locale.US, "%.1f", runway).toDoubleOrNull() ?: 6.5,
            liquidCashPercentage = if (totalAssets > 0) (liquidCash / totalAssets) * 100 else 15.0,
            riskExposure = "Optimal Alpha Balance",
            rebalancingActionList = listOf(
                "Automate $350/mo reallocation from Idle Cash to S&P500 Index / Technology ETF",
                "Lock in 5.15% APY High-Yield Cash sweep for emergency runway buffer",
                "Rebalance crypto exposure to under 12% to suppress portfolio volatility factor"
            ),
            projected10YearTotal = projections.lastOrNull()?.totalPortfolioValue ?: 250000.0
        )
    }

    private fun seedInitialData() {
        // Sample Bills with forensic analysis
        _bills.value = listOf(
            ScannedBill(
                id = "bill-1",
                providerName = "Pacific Grid & Power",
                category = "Utility",
                invoiceNumber = "PGP-988421",
                billingPeriod = "Jul 15 - Aug 14, 2026",
                dueDate = "Sep 02, 2026",
                subtotal = 164.20,
                taxes = 14.80,
                totalAmountDue = 197.40,
                lineItems = listOf(
                    BillLineItem("Base Residential Electric Delivery (420 kWh)", 92.40),
                    BillLineItem("Generation Service Charge Tier 1", 53.40),
                    BillLineItem("Peak Demand Surge Multiplier", 18.40, isFlagged = true, flagReason = "Exceeds State Energy Baseline Tariff ceiling by 18%"),
                    BillLineItem("Grid Modernization Mandate Rider", 14.80, isFlagged = true, flagReason = "Voluntary utility infrastructure rider - waiver eligible"),
                    BillLineItem("Local Municipal Utility Tax", 18.40)
                ),
                hiddenFees = listOf(
                    HiddenFee(
                        feeName = "Peak Demand Surge Multiplier",
                        amount = 18.40,
                        reason = "Arbitrary unannounced 1.4x multiplier applied outside statutory peak hours (2pm-7pm).",
                        severity = FeeSeverity.CRITICAL,
                        regulatoryClause = "CPUC Section 739.5 Tariff Overcharge Clause"
                    ),
                    HiddenFee(
                        feeName = "Grid Modernization Mandate Rider",
                        amount = 14.80,
                        reason = "Discretionary carrier surcharge disguised as state mandate.",
                        severity = FeeSeverity.MEDIUM,
                        regulatoryClause = "FTC Deceptive Energy Rider Protocol"
                    )
                ),
                forensicSummary = "Detected 2 stealth utility riders totaling $33.20/mo ($398.40/yr). High probability of 100% waiver upon submitting retention tariff challenge.",
                priceCreepPercentage = 16.4
            ),
            ScannedBill(
                id = "bill-2",
                providerName = "Apex Broadband & Fiber",
                category = "Telecom",
                invoiceNumber = "APX-0049219",
                billingPeriod = "Aug 01 - Aug 31, 2026",
                dueDate = "Sep 05, 2026",
                subtotal = 89.99,
                taxes = 7.50,
                totalAmountDue = 129.99,
                lineItems = listOf(
                    BillLineItem("Gigabit Fiber Symmetrical 1Gbps", 79.99),
                    BillLineItem("Promotional Discount (Expired Month 12)", 10.00, isFlagged = true, flagReason = "Auto-discount dropped silently"),
                    BillLineItem("Regulatory Cost Recovery Surcharge", 14.50, isFlagged = true, flagReason = "Internal ISP tax passed to consumer without contract notice"),
                    BillLineItem("WiFi 7 Mesh Extender Rental Fee", 18.00, isFlagged = true, flagReason = "Phantom equipment lease fee after purchased hardware"),
                    BillLineItem("State Universal Service Fund", 7.50)
                ),
                hiddenFees = listOf(
                    HiddenFee(
                        feeName = "Regulatory Cost Recovery Surcharge",
                        amount = 14.50,
                        reason = "Non-governmental carrier recovery markup.",
                        severity = FeeSeverity.CRITICAL,
                        regulatoryClause = "FCC Truth-in-Billing 47 CFR § 64.2401"
                    ),
                    HiddenFee(
                        feeName = "WiFi 7 Mesh Extender Rental Fee",
                        amount = 18.00,
                        reason = "Billed for customer-owned gateway equipment.",
                        severity = FeeSeverity.CRITICAL,
                        regulatoryClause = "Television & Broadband Viewer Protection Act 2020"
                    )
                ),
                forensicSummary = "Identified $32.50/mo in unauthorized phantom telecom charges. Step-by-step FCC negotiation script generated.",
                priceCreepPercentage = 22.8
            ),
            ScannedBill(
                id = "bill-3",
                providerName = "Metro General Health Care",
                category = "Medical",
                invoiceNumber = "MED-771029",
                billingPeriod = "Treatment Date: Jul 28, 2026",
                dueDate = "Sep 15, 2026",
                subtotal = 420.00,
                taxes = 0.00,
                totalAmountDue = 495.00,
                lineItems = listOf(
                    BillLineItem("Diagnostic Specialist Evaluation (CPT 99214)", 280.00),
                    BillLineItem("Standard Diagnostic Blood Work Panel", 140.00),
                    BillLineItem("Facility Resource Overhead Processing Rider", 75.00, isFlagged = true, flagReason = "Unbundled facility charge prohibited under No Surprises Act")
                ),
                hiddenFees = listOf(
                    HiddenFee(
                        feeName = "Facility Resource Overhead Processing Rider",
                        amount = 75.00,
                        reason = "Unbundled overhead fee charged on in-network outpatient visit.",
                        severity = FeeSeverity.CRITICAL,
                        regulatoryClause = "Federal No Surprises Act (45 CFR Part 149)"
                    )
                ),
                forensicSummary = "Hospital added a $75.00 facility fee in violation of federal surprise billing benchmarks. Action: Demand itemized review with coding audit.",
                priceCreepPercentage = 18.0
            ),
            ScannedBill(
                id = "bill-4",
                providerName = "Nexus Cloud SaaS Platform",
                category = "SaaS",
                invoiceNumber = "NX-882910",
                billingPeriod = "Sep 2026 - Annual Tier",
                dueDate = "Sep 18, 2026",
                subtotal = 240.00,
                taxes = 19.20,
                totalAmountDue = 289.20,
                lineItems = listOf(
                    BillLineItem("Developer Pro Team Seat (3 Users)", 180.00),
                    BillLineItem("Compute Compute Node Allocation Tier 2", 60.00),
                    BillLineItem("Legacy Support Maintenance Uplift", 30.00, isFlagged = true, flagReason = "Automatic annual 15% price hike without prior email opt-in"),
                    BillLineItem("State & Federal Cloud Tax", 19.20)
                ),
                hiddenFees = listOf(
                    HiddenFee(
                        feeName = "Legacy Support Maintenance Uplift",
                        amount = 30.00,
                        reason = "Silent subscription creep added on renewal date.",
                        severity = FeeSeverity.MEDIUM,
                        regulatoryClause = "FTC Negative Option Rule & Auto-Renew Transparency Act"
                    )
                ),
                forensicSummary = "Identified 15% unnotified subscription renewal creep. Can renegotiate to original tier pricing.",
                priceCreepPercentage = 15.0
            )
        )

        // Envelopes
        _envelopes.value = listOf(
            BudgetEnvelope("env-1", ExpenseCategory.HOUSING, allocatedMonthly = 1850.0, spentCurrentMonth = 1850.0),
            BudgetEnvelope("env-2", ExpenseCategory.FOOD, allocatedMonthly = 850.0, spentCurrentMonth = 628.40),
            BudgetEnvelope("env-3", ExpenseCategory.UTILITIES, allocatedMonthly = 350.0, spentCurrentMonth = 312.80),
            BudgetEnvelope("env-4", ExpenseCategory.TRANSPORT, allocatedMonthly = 400.0, spentCurrentMonth = 248.50),
            BudgetEnvelope("env-5", ExpenseCategory.TECH, allocatedMonthly = 280.0, spentCurrentMonth = 265.00),
            BudgetEnvelope("env-6", ExpenseCategory.HEALTH, allocatedMonthly = 200.0, spentCurrentMonth = 115.00),
            BudgetEnvelope("env-7", ExpenseCategory.ENTERTAINMENT, allocatedMonthly = 280.0, spentCurrentMonth = 190.30)
        )

        // Transactions
        _transactions.value = listOf(
            Transaction("tx-1", "BioMarket Whole Foods", "Whole Foods", ExpenseCategory.FOOD, 94.20, TransactionType.EXPENSE, "Today, 10:14 AM", "Weekly organic groceries"),
            Transaction("tx-2", "Pacific Grid & Power Surcharge", "Pacific Grid", ExpenseCategory.UTILITIES, 197.40, TransactionType.EXPENSE, "Yesterday", "Electric billing cycle (contains $33.20 phantom fees)", isFlaggedOvercharge = true),
            Transaction("tx-3", "Bi-Weekly Tech Direct Deposit", "CyberSys Corp", ExpenseCategory.INCOME, 3925.00, TransactionType.INCOME, "Aug 26, 2026", "Direct Payroll Deposit"),
            Transaction("tx-4", "Apex Fiber Internet", "Apex Broadband", ExpenseCategory.TECH, 129.99, TransactionType.EXPENSE, "Aug 24, 2026", "Monthly internet (Flagged $32.50 equipment fee)", isFlaggedOvercharge = true),
            Transaction("tx-5", "Metro Transit Card Reload", "Metro Rail", ExpenseCategory.TRANSPORT, 45.00, TransactionType.EXPENSE, "Aug 22, 2026", "Commuter pass"),
            Transaction("tx-6", "Cloud Storage Pro Double-Dip", "DriveSync Pro", ExpenseCategory.TECH, 14.99, TransactionType.EXPENSE, "Aug 20, 2026", "Overlapping backup storage subscription", isFlaggedOvercharge = true),
            Transaction("tx-7", "Green Leaf Cafe", "Green Leaf", ExpenseCategory.FOOD, 16.50, TransactionType.EXPENSE, "Aug 19, 2026", "Matcha latte & power bowl")
        )

        // Subscriptions
        _subscriptions.value = listOf(
            Subscription("sub-1", "StreamMax Ultra 4K", "Streaming Video", 22.99, hiddenFeeMonthly = 2.99, equipmentRentalMonthly = 0.0, expectedAnnualInflation = 0.08, active = true, usageFrequency = "Daily"),
            Subscription("sub-2", "CinePrime Cinema Pass", "Streaming Video", 17.99, hiddenFeeMonthly = 1.50, equipmentRentalMonthly = 0.0, expectedAnnualInflation = 0.06, active = true, usageFrequency = "Low (1x/month)"),
            Subscription("sub-3", "Apex Fiber Gigabit", "Telecom & Broadband", 89.99, hiddenFeeMonthly = 14.50, equipmentRentalMonthly = 18.00, expectedAnnualInflation = 0.05, active = true, usageFrequency = "Daily"),
            Subscription("sub-4", "CloudVault 2TB Storage", "Cloud Storage", 11.99, hiddenFeeMonthly = 0.0, equipmentRentalMonthly = 0.0, expectedAnnualInflation = 0.03, active = true, usageFrequency = "Daily"),
            Subscription("sub-5", "DriveSync Pro 1TB", "Cloud Storage", 9.99, hiddenFeeMonthly = 0.0, equipmentRentalMonthly = 0.0, expectedAnnualInflation = 0.03, active = true, usageFrequency = "Low (Duplicate Storage)"),
            Subscription("sub-6", "DevPulse AI Co-Pilot", "Productivity SaaS", 20.00, hiddenFeeMonthly = 0.0, equipmentRentalMonthly = 0.0, expectedAnnualInflation = 0.04, active = true, usageFrequency = "Daily"),
            Subscription("sub-7", "AeroFitness Gym Club", "Gym/Wellness", 65.00, hiddenFeeMonthly = 4.50, equipmentRentalMonthly = 10.00, expectedAnnualInflation = 0.07, active = true, usageFrequency = "Weekly")
        )

        // Asset Allocations
        _assetAllocations.value = listOf(
            AssetCategory("Equities & ETFs", 0.45, 66892.50, 0xFF06B6D4L, 0.092),
            AssetCategory("Real Estate REITs", 0.25, 37162.50, 0xFF10B981L, 0.075),
            AssetCategory("High-Yield Cash / Treasuries", 0.15, 22297.50, 0xFF3B82F6L, 0.051),
            AssetCategory("Digital Assets & Crypto", 0.10, 14865.00, 0xFF8B5CF6L, 0.145),
            AssetCategory("Commodities & Gold", 0.05, 7432.50, 0xFFF59E0BL, 0.062)
        )

        // Chat initial messages
        _chatMessages.value = listOf(
            ChatMessage(
                id = "msg-1",
                sender = MessageSender.ASSISTANT,
                text = "⚡ **Nexura AI Copilot Initialized.** I have completed a forensic scan of your accounts and invoices. Detected **$142.80/mo** in unbundled fees, phantom riders, and subscription overlap leaks. What would you like to optimize today?",
                timestamp = "11:00 AM",
                persona = CopilotPersona.FORENSIC_AUDITOR,
                actionChip = "Audit Pacific Power Bill ($33.20)",
                actionTargetTab = "scanner"
            )
        )
    }
}

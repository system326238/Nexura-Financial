package com.example.data.models

data class ScannedBill(
    val id: String,
    val providerName: String,
    val category: String, // "Utility", "Telecom", "Medical", "SaaS", "Insurance"
    val invoiceNumber: String,
    val billingPeriod: String,
    val dueDate: String,
    val subtotal: Double,
    val taxes: Double,
    val totalAmountDue: Double,
    val lineItems: List<BillLineItem>,
    val hiddenFees: List<HiddenFee>,
    val forensicSummary: String,
    val priceCreepPercentage: Double = 0.0,
    val isPaid: Boolean = false,
    val paymentReceipt: PaymentReceipt? = null
)

data class BillLineItem(
    val description: String,
    val amount: Double,
    val isFlagged: Boolean = false,
    val flagReason: String? = null
)

data class HiddenFee(
    val feeName: String,
    val amount: Double,
    val reason: String,
    val severity: FeeSeverity, // LOW, MEDIUM, CRITICAL
    val regulatoryClause: String
)

enum class FeeSeverity {
    LOW, MEDIUM, CRITICAL
}

enum class SettlementRail(val title: String, val speed: String, val feeDescription: String) {
    FEDNOW("FedNow Real-Time", "Instant (< 2.5s)", "Zero Surcharge Rail"),
    ACH_EXPRESS("ACH Next-Day", "24-Hour Settlement", "Standard Clearing"),
    DEBIT_DIRECT("Instant Debit Rails", "Immediate", "Direct Tokenized Surcharge-Free")
}

data class PaymentReceipt(
    val transactionId: String,
    val paidAmount: Double,
    val currencyCode: String,
    val settlementRail: SettlementRail,
    val timestamp: String,
    val status: String = "SETTLED_ON_CHAIN_LEDGER"
)

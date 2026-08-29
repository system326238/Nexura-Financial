package com.example.data.models

enum class TransactionType {
    INCOME, EXPENSE
}

enum class ExpenseCategory(val label: String, val iconName: String) {
    HOUSING("Housing & Rent", "Home"),
    FOOD("Food & Dining", "Restaurant"),
    UTILITIES("Utilities & Power", "Bolt"),
    TRANSPORT("Transport & Auto", "DirectionsCar"),
    TECH("Tech & Subscriptions", "Devices"),
    HEALTH("Health & Medical", "MedicalServices"),
    ENTERTAINMENT("Entertainment", "SportsEsports"),
    INCOME("Income / Salary", "AccountBalanceWallet")
}

data class BudgetEnvelope(
    val id: String,
    val category: ExpenseCategory,
    val allocatedMonthly: Double, // in USD
    val spentCurrentMonth: Double // in USD
) {
    val remaining: Double get() = (allocatedMonthly - spentCurrentMonth).coerceAtLeast(0.0)
    val usagePercentage: Float get() = if (allocatedMonthly > 0) (spentCurrentMonth / allocatedMonthly).toFloat() else 0f
    val isOverBudget: Boolean get() = spentCurrentMonth > allocatedMonthly
    val isNearLimit: Boolean get() = spentCurrentMonth >= (allocatedMonthly * 0.85) && !isOverBudget
}

data class Transaction(
    val id: String,
    val title: String,
    val merchant: String,
    val category: ExpenseCategory,
    val amountUsd: Double,
    val type: TransactionType,
    val date: String,
    val note: String = "",
    val isFlaggedOvercharge: Boolean = false
)

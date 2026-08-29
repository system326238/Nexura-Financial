package com.example.data.models

enum class RiskProfile(val displayName: String, val expectedAnnualReturn: Double, val volatility: String) {
    CONSERVATIVE("Conservative (Defensive Yield)", 0.055, "Low (3.2%)"),
    MODERATE("Moderate (Balanced Growth)", 0.085, "Medium (8.4%)"),
    AGGRESSIVE("Aggressive (Alpha Tech & Crypto)", 0.125, "High (16.8%)")
}

data class AssetCategory(
    val name: String,
    val allocationPercentage: Double, // 0.0 to 1.0
    val totalAmountUsd: Double,
    val colorHex: Long,
    val yieldPercentage: Double
)

data class CompoundingYearData(
    val year: Int,
    val totalPrincipal: Double,
    val totalInterestCompounded: Double,
    val totalPortfolioValue: Double
)

data class PortfolioDiagnostic(
    val diversificationScore: Int, // 0 - 100
    val emergencyRunwayMonths: Double,
    val liquidCashPercentage: Double,
    val riskExposure: String,
    val rebalancingActionList: List<String>,
    val projected10YearTotal: Double
)

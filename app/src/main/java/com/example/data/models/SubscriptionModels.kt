package com.example.data.models

data class Subscription(
    val id: String,
    val name: String,
    val category: String, // "Streaming Video", "Music", "Cloud Storage", "Productivity SaaS", "Gym/Wellness"
    val statedMonthlyPrice: Double,
    val billingCycle: String = "Monthly",
    val hiddenFeeMonthly: Double = 0.0,
    val equipmentRentalMonthly: Double = 0.0,
    val expectedAnnualInflation: Double = 0.045, // 4.5% annual creep
    val active: Boolean = true,
    val renewalDate: String = "15th of each month",
    val usageFrequency: String = "Low (2x/month)" // "Daily", "Weekly", "Low"
) {
    // Truth Cost computations
    val actualMonthlyCost: Double get() = statedMonthlyPrice + hiddenFeeMonthly + equipmentRentalMonthly
    val truthAnnualCost: Double get() = actualMonthlyCost * 12 * (1 + expectedAnnualInflation)
    val truthThreeYearCost: Double get() {
        val y1 = actualMonthlyCost * 12
        val y2 = y1 * (1 + expectedAnnualInflation)
        val y3 = y2 * (1 + expectedAnnualInflation)
        return y1 + y2 + y3
    }
}

data class SubscriptionOverlapGroup(
    val category: String,
    val subscriptions: List<Subscription>,
    val recommendation: String,
    val potentialAnnualSavings: Double
)

data class RegulatoryBenchmark(
    val serviceType: String,
    val averageMarketPrice: Double,
    val statedPrice: Double,
    val truthPrice: Double,
    val benchmarkScore: BenchmarkRating, // FAIR, ELEVATED, PREDATORY
    val benchmarkNotes: String
)

enum class BenchmarkRating {
    FAIR, ELEVATED, PREDATORY
}

data class NegotiationScript(
    val id: String,
    val targetProvider: String,
    val targetFeeName: String,
    val estimatedAnnualSavings: Double,
    val scriptSteps: List<ScriptStep>,
    val regulatoryBackingClauses: List<String>
)

data class ScriptStep(
    val stepNumber: Int,
    val speakerRole: String, // "User (Opening)", "User (Counter)", "User (Escalation / Close)"
    val scriptText: String,
    val tacticalTip: String
)

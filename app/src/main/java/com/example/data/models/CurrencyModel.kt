package com.example.data.models

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val rateAgainstUsd: Double // exchange rate: 1 USD = rate units
) {
    USD("USD", "$", "US Dollar", 1.0),
    EUR("EUR", "€", "Euro", 0.92),
    GBP("GBP", "£", "British Pound", 0.79),
    JPY("JPY", "¥", "Japanese Yen", 154.20),
    CAD("CAD", "CA$", "Canadian Dollar", 1.36),
    AUD("AUD", "A$", "Australian Dollar", 1.52),
    INR("INR", "₹", "Indian Rupee", 83.45),
    CHF("CHF", "CHF", "Swiss Franc", 0.90),
    SGD("SGD", "S$", "Singapore Dollar", 1.35),
    AED("AED", "AED", "UAE Dirham", 3.67);

    fun format(amountInUsd: Double): String {
        val converted = amountInUsd * rateAgainstUsd
        return when (this) {
            JPY -> String.format("%s%,.0f", symbol, converted)
            INR -> String.format("%s%,.0f", symbol, converted)
            CHF, AED -> String.format("%s %,.2f", symbol, converted)
            else -> String.format("%s%,.2f", symbol, converted)
        }
    }

    fun convertFromUsd(amountInUsd: Double): Double = amountInUsd * rateAgainstUsd
    fun convertToUsd(amountInCurrency: Double): Double = amountInCurrency / rateAgainstUsd
}

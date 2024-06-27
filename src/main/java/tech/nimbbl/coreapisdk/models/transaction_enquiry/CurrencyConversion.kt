package tech.nimbbl.coreapisdk.models.transaction_enquiry

data class CurrencyConversion(
    val conversion_reason: String,
    val converted_currency: String,
    val exchange_rate: Double,
    val original_amount_before_tax: Double,
    val original_currency: String,
    val original_tax: Double,
    val original_total_amount: Double
)
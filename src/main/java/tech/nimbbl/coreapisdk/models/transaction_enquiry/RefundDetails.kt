package tech.nimbbl.coreapisdk.models.transaction_enquiry

data class RefundDetails(
    val available_refundable_amount: Int,
    val refundable_currency: String,
    val refunded_amount: Int,
    val total_refundable_amount: Int
)
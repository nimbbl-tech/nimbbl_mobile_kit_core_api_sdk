package tech.nimbbl.coreapisdk.models.transaction_enquiry

data class Order(
    val currency_conversion: CurrencyConversion,
    val custom_attributes: List<CustomAttribute>,
    val invoice_id: String,
    val nimbbl_order_id: String,
    val refund_details: RefundDetails,
    val status: String
)
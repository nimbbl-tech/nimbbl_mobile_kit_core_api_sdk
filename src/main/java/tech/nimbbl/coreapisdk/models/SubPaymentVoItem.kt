package tech.nimbbl.coreapisdk.models

data class SubPaymentVoItem(
    val display_priority: Int,
    val logo_url: String?,
    val sub_payment_code: String,
    val sub_payment_name: String
)
package tech.nimbbl.coreapisdk.data.models.common

data class RequestArgs(
    val order_id: String,
    val payment_mode: String,
    val transaction_id: String
)
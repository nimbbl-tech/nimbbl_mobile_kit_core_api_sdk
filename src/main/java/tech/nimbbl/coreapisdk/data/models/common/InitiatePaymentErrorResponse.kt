package tech.nimbbl.coreapisdk.data.models.common


data class InitiatePaymentErrorResponse(
    val error: Error,
    val status_code: Int
)
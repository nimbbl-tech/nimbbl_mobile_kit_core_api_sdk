package tech.nimbbl.coreapisdk.models


data class InitiatePaymentErrorResponse(
    val error: Error,
    val status_code: Int
)
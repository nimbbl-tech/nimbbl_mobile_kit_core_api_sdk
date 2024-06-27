package tech.nimbbl.coreapisdk.models

data class InitiatePaymentExtraInfo(
    val attempts: Int,
    val `data`: InitiatePaymentData,
    val payment_partner: String
)
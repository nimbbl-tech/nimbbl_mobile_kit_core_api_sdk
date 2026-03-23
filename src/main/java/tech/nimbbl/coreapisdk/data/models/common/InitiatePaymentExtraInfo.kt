package tech.nimbbl.coreapisdk.data.models.common

data class InitiatePaymentExtraInfo(
    val attempts: Int?,
    val `data`: InitiatePaymentData?,
    val payment_partner: String?
)
package tech.nimbbl.coreapisdk.data.models.common

data class ResendOtpResponse(
    val otp_sent: Boolean?,
    val status_code: Int?,
    val success: Boolean?
)
package tech.nimbbl.coreapisdk.models

data class ResendOtpResponse(
    val otp_sent: Boolean,
    val status_code: Int,
    val success: Boolean
)
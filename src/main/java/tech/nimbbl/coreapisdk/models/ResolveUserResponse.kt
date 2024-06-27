package tech.nimbbl.coreapisdk.models

data class ResolveUserResponse(
    val item: User,
    val next_step: String,
    val otp_sent: Boolean,
    val success: Boolean
)
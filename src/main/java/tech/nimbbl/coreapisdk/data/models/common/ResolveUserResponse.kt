package tech.nimbbl.coreapisdk.data.models.common

import tech.nimbbl.coreapisdk.data.models.user.User

data class ResolveUserResponse(
    val item: User?,
    val next_step: String?,
    val otp_sent: Boolean?,
    val success: Boolean?
)
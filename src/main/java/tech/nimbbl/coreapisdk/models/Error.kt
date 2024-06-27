package tech.nimbbl.coreapisdk.models

data class Error(
    val attempts: Int,
    val c_message: String,
    val code: String,
    val m_message: String,
    val raise_alarm: Boolean,
    val retry_allowed: Boolean,
    val status: String,
    val status_code: Int
)
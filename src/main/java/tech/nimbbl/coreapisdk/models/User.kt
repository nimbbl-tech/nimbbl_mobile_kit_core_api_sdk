package tech.nimbbl.coreapisdk.models

data class User(
    val country_code: String,
    val email: String,
    val first_name: String,
    val id: String,
    val last_name: String?,
    val mobile_number: String,
    val token: String,
    val token_expiration: String,
    val user_id: String
)
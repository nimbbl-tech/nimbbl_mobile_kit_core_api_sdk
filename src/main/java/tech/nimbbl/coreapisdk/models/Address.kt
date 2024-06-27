package tech.nimbbl.coreapisdk.models

data class Address(
    val address_1: String,
    val address_id: String,
    val address_type: String,
    val area: String,
    val city: String,
    val landmark: String,
    val pincode: String,
    val state: String,
    val street: String
)
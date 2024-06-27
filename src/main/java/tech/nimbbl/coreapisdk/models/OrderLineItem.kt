package tech.nimbbl.coreapisdk.models

data class OrderLineItem(
    val amount_before_tax: Any,
    val description: String,
    val image_url: String,
    val order_id: Int,
    val quantity: Int,
    val rate: Double,
    val sku_id: Any,
    val tax: Any,
    val title: String,
    val total_amount: Double,
    val uom: Any
)
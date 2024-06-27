package tech.nimbbl.coreapisdk.models

data class Item(
    val display_priority: Int?,
    val display_tray: String?,
    var flow: String?,
    val items: Any?,
    var logo_url: String?,
    val payment_mode: String?,
    val payment_mode_code: String?,
    val sub_payment_code: String?,
    val sub_payment_name: String?,
    val app_code: String?,
    val app_name: String?,
    val app_package_name: String?,
    val extraInfo: ExtraInfo?,
    val additional_charges: String?,
    val next: String?,
    var itemCol: List<Item>?,
)
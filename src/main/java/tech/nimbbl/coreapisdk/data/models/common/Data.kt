package tech.nimbbl.coreapisdk.data.models.common

import tech.nimbbl.coreapisdk.data.models.order.Item

data class Data(
    val display_tray: String,
    var items: List<Item>?
)
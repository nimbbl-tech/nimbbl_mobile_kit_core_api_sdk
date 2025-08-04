package tech.nimbbl.coreapisdk.data.models.payment

import tech.nimbbl.coreapisdk.data.models.common.ExtraInfo

data class PaymentModesResponseItem(
    val extra_info: ExtraInfo?,
    val payment_mode: String?
)
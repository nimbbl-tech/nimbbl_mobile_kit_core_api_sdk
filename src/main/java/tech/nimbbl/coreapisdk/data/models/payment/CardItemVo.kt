package tech.nimbbl.coreapisdk.data.models.payment

import tech.nimbbl.coreapisdk.data.models.order.Scheme
import tech.nimbbl.coreapisdk.data.models.user.Geography

data class CardItemVo(
    val card_type: List<CardType>,
    val geography: List<Geography>,
    val payment_types: List<PaymentType>,
    val schemes: List<Scheme>
)
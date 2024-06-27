package tech.nimbbl.coreapisdk.models

data class CardItemVo(
    val card_type: List<CardType>,
    val geography: List<Geography>,
    val payment_types: List<PaymentType>,
    val schemes: List<Scheme>
)
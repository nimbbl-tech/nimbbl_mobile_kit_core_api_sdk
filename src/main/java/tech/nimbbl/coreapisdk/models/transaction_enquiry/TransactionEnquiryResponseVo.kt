package tech.nimbbl.coreapisdk.models.transaction_enquiry

data class TransactionEnquiryResponseVo(
    val order: Order,
    val transaction: List<Transaction>
)
package tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry

data class TransactionEnquiryResponseVo(
    val order: Order,
    val transaction: List<Transaction>
)
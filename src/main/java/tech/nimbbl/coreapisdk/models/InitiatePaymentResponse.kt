package tech.nimbbl.coreapisdk.models

data class InitiatePaymentResponse(
    val completion_time: Float?,
    val extra_info: InitiatePaymentExtraInfo?,
    val info: Info?,
    val message: String?,
    val order_id: String?,
    val redirect_url: String?,
    val status: String?,
    val status_code: Int?,
    val transaction_id: String?,
    val vpa:String?,
    val isVPAValid: Int?,
    val payerAccountName: String?

)
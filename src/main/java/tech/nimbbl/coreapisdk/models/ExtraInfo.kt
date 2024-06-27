package tech.nimbbl.coreapisdk.models

data class ExtraInfo(
    val additional_charges: String?,
    val auto_debit_flow_possible: String?,
    val redirection_url: String?,
    val eta_completion: Int?,
    val next: String?,
    val vpa_account_holder: String?,
    var vpa_id: String?,
    val vpa_provider: String?,
    var app_package_name: String,
    val payment_type: String?,
    val server_intent: Boolean?
)
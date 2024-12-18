package tech.nimbbl.coreapisdk.models.checkout_details

data class CheckoutDetailsResponseVo(
    val change_user_allowed: Boolean,
    val checkout_background_color: String,
    val checkout_session_duration: Int,
    val checkout_text_color: String,
    val continue_btn_is_white: Boolean,
    val description: String,
    val display_name: String,
    val enable_desktop_view: Boolean,
    val fast_payment_enabled: Boolean,
    val ica_allowed: Boolean,
    val logo_file_url: Any,
    val mca_allowed: Boolean,
    val order_summary_enabled: Boolean,
    val other_payment_enabled: Boolean,
    val payment_experience: Any,
    val sub_merchant_id: String,
    val tokenization_enabled: Boolean
)
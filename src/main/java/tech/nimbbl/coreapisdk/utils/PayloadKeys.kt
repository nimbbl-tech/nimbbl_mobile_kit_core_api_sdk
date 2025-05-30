package tech.nimbbl.coreapisdk.utils

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

class PayloadKeys {

    companion object {

        const val key_token ="token"
        const val key_action ="action"
        const val key_packageName ="packageName"
        const val key_nimbblPayload ="nimbblPayload"
        const val key_OrderID ="order_id"
        const val key_event ="event"
        const val key_status ="status"
        const val key_next ="next"
        const val key_errorMessage ="errorMessage"
        const val key_message ="message"
        const val key_errorCode ="errorCode"
        const val key_mobileNumber ="mobile_number"
        const val key_deviceVerified ="device_verified"
        const val key_countryCode ="country_code"
        const val key_userAgent ="user_agent"
        const val key_ipAddress ="ipAddress"
        const val key_fingerPrint ="fingerPrint"
        const val key_domainName ="domain_name"
        const val key_paymentModes ="paymentModes"
        const val key_payment_status ="payment_status"
        const val key_transaction_id ="transaction_id"
        const val key_flow ="flow"
        const val key_signature ="signature"
        const val key_otp_sent ="otp_sent"
        const val key_vpa_id ="vpa_id"
        const val key_vpa_valid ="isVPAValid"
        const val key_vpa_account_holder ="vpa_account_holder"
        const val key_datetime ="datetime"
        const val key_product_id ="product_id"

        const val key_sub_payment_mode ="sub_payment_mode"
        const val key_payment_mode ="payment_mode"
        const val key_payment_type ="payment_type"
        const val key_bank ="bank"
        const val key_callback_url ="callback_url"
        const val key_next_step ="next_step"
        const val key_user_name ="key_user_name"
        const val key_otp ="otp"
        const val key_card_detail ="card_details"
        const val key_upi_id ="upi_id"
        const val key_app_package_name ="app_package_name"

        const val key_issuer_name = "issuerName"
        const val key_sub_payment_code = "sub_payment_code"
        const val key_sub_payment_name = "sub_payment_name"
        const val key_scheme_name = "schemeName"

        const val key_card_number ="card_no"
        const val key_card_cvv="cvv"
        const val key_card_holder_name="card_holder_name"
        const val key_card_expiry="expiry"

        const val key_nimbbl_error_code ="nimbbl_error_code"
        const val key_nimbbl_consumer_message ="nimbbl_consumer_message"
        const val key_nimbbl_merchant_message ="nimbbl_merchant_message"

        const val key_callback_mode ="callback_mode"
        const val key_referrer_platform ="referrer_platform"
        const val key_referrer_platform_version ="referrer_platform_version"

        const val key_invoice ="invoice_id"


        const val action_resolveUser ="resolve_user"
        const val action_verifyUser ="verify_user"

        const val action_initiateOrder ="initiate_order"
        const val action_paymentModes ="paymentModes"
        const val action_getBinData ="getbinData"
        const val action_validateCard ="validateCard"
        const val action_initiatePayment ="initiatePayment"
        const val action_paymentEnquiry ="paymentEnquiry"
        const val action_completePayment ="completePayment"
        const val action_resendOtp ="resendOtp"

        const val event_display_loader = "display_loader"
        const val event_hide_loader = "hide_loader"
        const val event_process_result = "process_result"
        const val event_exception_occured = "exception_occured"


        const val  status_success ="success"
        const val value_step_payment_mode = "payment_mode"
        const val value_step_otp_validation = "otp_validation"


        const val value_payment_mode_credit_card ="Credit Card"
        const val value_payment_mode_debit_card ="Debit Card"
        const val value_payment_mode_prepaid_card ="Prepaid Card"
        const val value_payment_mode_netbanking ="Netbanking"
        const val value_payment_mode_upi ="UPI"
        const val value_payment_mode_wallet ="Wallet"
        const val value_payment_mode_card ="Card"

        const val value_upi_flow_mode_magic ="magic"
        const val value_upi_flow_mode_collect ="collect"
        const val value_upi_flow_mode_server_intent ="server_intent"

        const val value_payment_mode_lazypay = "Lazypay"
        const val value_payment_mode_simpl = "Simpl"
        const val value_payment_mode_icici_paylater = "ICICI PayLater"
        const val value_payment_mode_olamoney = "Olamoney"

        const val value_payment_status_pending = "pending"
        const val value_payment_status_success = "success"
        const val value_payment_status_failed = "failed"
        const val value_payment_initiated= "Payment initiated"

        const val  value_payment_type_otp ="otp"
        const val  value_payment_type_auto_debit ="auto_debit"
        const val  value_payment_type_app_redirect ="app-redirect"

        const val value_sub_payment_code_credit = "credit"
        const val value_sub_payment_code_debit = "debit"
        const val value_sub_payment_code_prepaid = "prepaid"

        const val key_transaction_enquiry_api_response = "sdk_transaction_enquiry_response"
        const val key_upi_intent_back_response = "sdk_upi_intent_response"


        const val key_sdk_action = "sdk_action"
        const val key_source = "source"






    }
}
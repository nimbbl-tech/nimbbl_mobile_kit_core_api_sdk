package tech.nimbbl.coreapisdk.api

class ServiceConstants {

    companion object {

        /*  var BASE_URL = "https://uatapi.nimbbl.tech/api/v2/"*/
        var BASE_URL = "https://api.nimbbl.tech/"
        var WEB_VIEW_RESP_CHECK_URL = "https://checkout.nimbbl.tech/payment/response"
        var FINGERPRINT = ""
        var DEVICE_FINGERPRINT = ""
        const val CHECKOUT_CANCEL ="api/internal/checkout/cancel"
        const val CHECKOUT_DETAILS ="api/internal/checkout/details"

        const val VERIFY_DOMAIN = "verify-domain"
        const val CHECKOUT_RESOURCE = "checkout-resources"
        const val LIST_OF_BANKS = "list-of-banks"
        const val LIST_OF_WALLET = "list-of-wallets"
        const val PAYMENT_MODES = "payment-modes"
        const val INITIATE_PAYMENT = "initiate-payment"
        const val TRANSACTION_ENQUIRY = "api/v3/transaction-enquiry"
        const val GET_ORDER = "get-order/%1\$s"
        const val RESOLVE_USER = "resolve-user"
        const val VERIFY_USER = "https://api.nimbbl.tech/api/user/verify-otp"
        const val GET_PUBLIC_KEY = "get-nimbbl-public-key"
        const val GET_BIN_DATA= "get-bin-data"
        const val MAKE_PAYMENT= "payment"
        const val RESEND_OTP= "resend-otp"
        const val UPDATE_TRANSACTION= "update-transaction/%1\$s"
        const val  UPDATE_ORDER  = "api/v3/order"
    }
}
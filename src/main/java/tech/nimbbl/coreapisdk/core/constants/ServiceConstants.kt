package tech.nimbbl.coreapisdk.core.constants

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

class ServiceConstants {

    companion object {

        /*  var BASE_URL = "https://uatapi.nimbbl.tech/api/v2/"*/
        var BASE_URL = "https://api.nimbbl.tech/"
        var WEB_VIEW_RESP_CHECK_URL = "https://checkout.nimbbl.tech/payment/response"
        var FINGERPRINT = ""
        var DEVICE_FINGERPRINT = ""
        const val CHECKOUT_CANCEL ="api/internal/checkout/cancel"
        
        // Event Logging URLs
        const val EVENT_LOG_URL_QA = "https://eventlogpipepp.nimbbl.tech/v1/log"
        const val EVENT_LOG_URL_PP = "https://eventlogpipepp.nimbbl.tech/v1/log"
        const val EVENT_LOG_URL_PROD = "https://eventlogpipe.nimbbl.tech/v1/log"
        
        // Tenant IDs
        const val TENANT_ID_QA = "99df59c6-7553-49ab-b884-6a7a8dc3953b"
        const val TENANT_ID_PP = "5fd6a596-a39f-4cb2-9a4b-ed72713e537e"
        const val TENANT_ID_PROD = "6e7e900c-9ce6-4271-b1ec-08875a9129d1"

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
        
        // Event Logging Configuration - Dynamic based on BASE_URL
        
        /**
         * Get event log URL based on BASE_URL environment
         */
        fun getEventLogUrl(): String {
            return when {
                BASE_URL.contains("qa") -> {
                    EVENT_LOG_URL_QA
                }
                BASE_URL.contains("pp") -> {
                    EVENT_LOG_URL_PP
                }
                BASE_URL.contains("api.nimbbl.tech") && !BASE_URL.contains("qa") && !BASE_URL.contains("pp") -> {
                    EVENT_LOG_URL_PROD
                }
                else -> {
                    // Fallback to production
                    EVENT_LOG_URL_PROD
                }
            }
        }
        
        /**
         * Get default tenant ID based on BASE_URL environment
         */
        fun getDefaultTenantId(): String {
            return when {
                BASE_URL.contains("qa") -> {
                    TENANT_ID_QA
                }
                BASE_URL.contains("pp") -> {
                    TENANT_ID_PP
                }
                BASE_URL.contains("api.nimbbl.tech") && !BASE_URL.contains("qa") && !BASE_URL.contains("pp") -> {
                    TENANT_ID_PROD
                }
                else -> {
                    // Fallback to production
                    TENANT_ID_PROD
                }
            }
        }
    }
}
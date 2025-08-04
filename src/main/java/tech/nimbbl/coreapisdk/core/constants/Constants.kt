package tech.nimbbl.coreapisdk.core.constants

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import tech.nimbbl.coreapisdk.BuildConfig
import android.util.Log

object Constants {
    // Debug flag - automatically false in release builds
    val is_debug_enabled: Boolean
        get() = try {
            BuildConfig.DEBUG
        } catch (e: Exception) {
            Log.w("Constants", "Error accessing BuildConfig.DEBUG: ${e.message}", e)
            false // Default to false for safety
        }
    
    // Centralized SDK Version - Single source of truth for Core API SDK
    const val SDK_VERSION_CENTRAL = "3.0.8"
    const val sdk_version = SDK_VERSION_CENTRAL // Backward compatibility
    
    // SDK Initialization Errors
    const val ERROR_CODE_INIT_FAILED = "INIT_001"
    const val ERROR_MESSAGE_INIT_FAILED = "SDK Initialise Failed, Pass a valid reference and retry"
    const val NIMBBL_ERROR_CODE_INIT_FAILED = "MERCHANT_SDK_REFERENCE_INVALID"
    const val CUSTOMER_MESSAGE_INIT_FAILED = "There is some technical issue, kindly reach out to the merchant"
    
    // Generic Errors
    const val ERROR_CODE_UNKNOWN_ERROR = "UNKNOWN_001"
    const val ERROR_MESSAGE_UNKNOWN_ERROR = "There seems to be some issue , Please try after sometime.. use above to send the object  in response"
    const val NIMBBL_ERROR_CODE_UNKNOWN_ERROR = "MERCHANT_SDK_ERROR"
    const val CUSTOMER_MESSAGE_UNKNOWN_ERROR = "There is some technical issue, kindly reach out to the merchant"
    
    const val ERROR_CODE_API_URL_NULL = "API_URL_001"
    const val ERROR_MESSAGE_API_URL_NULL = "API URL is null or empty. Please provide a valid environment URL."
    const val NIMBBL_ERROR_CODE_API_URL_NULL = "MERCHANT_SDK_ERROR"
    const val CUSTOMER_MESSAGE_API_URL_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    // Checkout Start Errors
    const val ERROR_CODE_CHECKOUT_REFERENCE_NULL = "CHECKOUT_001"
    const val ERROR_MESSAGE_CHECKOUT_REFERENCE_NULL = "Reference is null"
    const val ERROR_MESSAGE_CHECKOUT_REFERENCE_NULL_MERCHANT = "CHeckout Failed, reference element is missing"
    const val NIMBBL_ERROR_CODE_CHECKOUT_REFERENCE_NULL = "MERCHANT_SDK_REFERENCE_MISSING"
    const val CUSTOMER_MESSAGE_CHECKOUT_REFERENCE_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    const val ERROR_CODE_CHECKOUT_LISTENER_NULL = "CHECKOUT_002"
    const val ERROR_MESSAGE_CHECKOUT_LISTENER_NULL = "Listener is null"
    const val ERROR_MESSAGE_CHECKOUT_LISTENER_NULL_MERCHANT = "Checkout listener is missing, initialize SDK properly"
    const val NIMBBL_ERROR_CODE_CHECKOUT_LISTENER_NULL = "MERCHANT_SDK_REFERENCE_MISSING"
    const val CUSTOMER_MESSAGE_CHECKOUT_LISTENER_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    const val ERROR_CODE_CHECKOUT_OPTIONS_NULL = "CHECKOUT_003"
    const val ERROR_MESSAGE_CHECKOUT_OPTIONS_NULL = "Options is null"
    const val ERROR_MESSAGE_CHECKOUT_OPTIONS_NULL_MERCHANT = "Checkout options missing , pass a valid option object"
    const val NIMBBL_ERROR_CODE_CHECKOUT_OPTIONS_NULL = "MERCHANT_SDK_OPTIONS_MISSING"
    const val CUSTOMER_MESSAGE_CHECKOUT_OPTIONS_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    const val ERROR_CODE_CHECKOUT_TOKEN_NULL = "CHECKOUT_004"
    const val ERROR_MESSAGE_CHECKOUT_TOKEN_NULL = "Options token is null"
    const val ERROR_MESSAGE_CHECKOUT_TOKEN_NULL_MERCHANT = "Checkout token is missing, pass valid options.token and retry"
    const val NIMBBL_ERROR_CODE_CHECKOUT_TOKEN_NULL = "MERCHANT_SDK_TOKEN_MISSING"
    const val CUSTOMER_MESSAGE_CHECKOUT_TOKEN_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    const val ERROR_CODE_CHECKOUT_ORDER_ID_NULL = "CHECKOUT_005"
    const val ERROR_MESSAGE_CHECKOUT_ORDER_ID_NULL = "Options token -> orderId is null"
    const val ERROR_MESSAGE_CHECKOUT_ORDER_ID_NULL_MERCHANT = "Order ID missing inside checkout token."
    const val NIMBBL_ERROR_CODE_CHECKOUT_ORDER_ID_NULL = "MERCHANT_SDK_ORDERID_MISSING"
    const val CUSTOMER_MESSAGE_CHECKOUT_ORDER_ID_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    const val ERROR_CODE_CHECKOUT_SUBMERCHANT_ID_NULL = "CHECKOUT_006"
    const val ERROR_MESSAGE_CHECKOUT_SUBMERCHANT_ID_NULL = "Options token -> subMerchantId is null"
    const val ERROR_MESSAGE_CHECKOUT_SUBMERCHANT_ID_NULL_MERCHANT = "Sub-merchant id missing"
    const val NIMBBL_ERROR_CODE_CHECKOUT_SUBMERCHANT_ID_NULL = "MERCHANT_SDK_SMID_MISSING"
    const val CUSTOMER_MESSAGE_CHECKOUT_SUBMERCHANT_ID_NULL = "There is some technical issue, kindly reach out to the merchant"
    
    const val access_key_allowed_false = "Access not allowed"
    const val sww = "Something went wrong. Please try again."
    const val no_internet = "Please check your internet connection"
    const val domain_not_allowed = "Access Key Not Allowed To Be Accessed From This Domain"
    const val payment_status_code_cancelled = "CUSTOMER_PAYMENT_CANCELLED"
    const val payment_status_message_cancelled = "Customer has cancelled the payment."
    const val error_in_update_order = "Error with response Code: while updating order"
    
    // Event Logging Configuration - Dynamic based on BASE_URL with null safety
    val EVENT_LOG_URL: String
        get() = try {
            ServiceConstants.getEventLogUrl()
        } catch (e: Exception) {
            Log.w("Constants", "Error getting event log URL: ${e.message}", e)
            "" // Return empty string as fallback
        }
    
    val DEFAULT_TENANT_ID: String
        get() = try {
            ServiceConstants.getDefaultTenantId()
        } catch (e: Exception) {
            Log.w("Constants", "Error getting default tenant ID: ${e.message}", e)
            "" // Return empty string as fallback
        }
}
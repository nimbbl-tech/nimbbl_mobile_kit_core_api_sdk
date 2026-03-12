package tech.nimbbl.coreapisdk.core.constants

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

/**
 * Event constants for Nimbbl Core API SDK event logging
 */
object EventConstants {
    
    // SDK Events
    const val SDK_INITIALIZED = "sdk_initialized"
    
    // Checkout Events
    const val CHECKOUT_INITIATED = "checkout_initiated"
    const val UPDATE_ORDER_PAYLOAD = "update_order_payload"
    
    // WebView Events
    const val WEBVIEW_LAUNCH = "webview_launch"
    const val WEBVIEW_LOADED = "webview_loaded"
    const val WEBVIEW_FAILED = "webview_failed"
    const val BACK_BUTTON_PRESSED = "back_button_pressed"

    const val REDIRECT_URL_HIT = "redirect_url_hit"
    const val URL_INTERCEPTED = "url_intercepted"
    
    // Exception Events
    const val EXCEPTION_THROWN = "exception_thrown"
    
    // UPI Events
    const val UPI_APPS_LISTED = "upi_apps_listed"
    const val UPI_INTENT_STARTED = "upi_intent_started"
    const val UPI_INTENT_CLOSED = "upi_intent_closed"
    
    // Payment Events
    const val PAYMENT_STATUS = "payment_status"

    // Event payload keys
    const val KEY_SUBMERCHANT_ID = "submerchantId"
    const val KEY_NAME = "name"
    const val KEY_TRANSACTION_ID = "transactionId"
    const val KEY_ORDER_ID = "orderId"
    const val KEY_VERSION = "version"
    const val KEY_CLIENT_IP = "clientIP"
    const val KEY_PRODUCT_CODE = "productCode"
    const val KEY_PRODUCT_NAME = "product_name"
    const val KEY_APPLICATION_NAME = "application_name"
    const val KEY_EVENT_TIMESTAMP = "eventTimestamp"
    const val KEY_DATA = "data"
    const val KEY_UA = "ua"
    const val KEY_UA_BROWSER = "ua_browser"
    const val KEY_UA_CPU = "ua_cpu"
    const val KEY_UA_DEVICE = "ua_device"
    const val KEY_UA_ENGINE = "ua_engine"
    const val KEY_UA_OS = "ua_os"
    const val KEY_ENVIRONMENT = "environment"
    const val KEY_APP_VERSION = "appVersion"
    const val KEY_ORDER_ID_DATA = "order_id"
    const val KEY_TOKEN = "token"
    const val KEY_DEVICE_ID = "device_id"
    const val KEY_CARRIER = "carrier"
    const val KEY_CARRIER_TYPE = "carrier_type"
    const val KEY_IP_ADDRESS = "ip_address"
    const val KEY_EXPIRY_DATE = "expiryDate"
    const val KEY_EVENT_TIMESTAMP_DATA = "event_timestamp"
    const val KEY_EVENT_TIMEZONE = "event_timezone"
    const val KEY_EVENT_TIMEZONE_OFFSET = "event_timezone_offset"
    const val KEY_EVENT_TIME_LOCAL = "event_time_local"
    const val KEY_EVENT_TIME_UTC = "event_time_utc"
    const val KEY_TOKEN_EXPIRY_DATE = "token_expiry_date"
    const val KEY_LOGGING_ERROR = "logging_error"

    // WebView event keys
    const val KEY_URL = "url"
    const val KEY_LOADED_URL = "loaded_url"
    const val KEY_INTERCEPTED_URL = "intercepted_url"
    const val KEY_ERROR_CODE = "error_code"
    const val KEY_ERROR_DESCRIPTION = "error_description"

    // UPI event keys
    const val KEY_APPS_LIST = "apps_list"

    // Device info keys (used by getDeviceInfo / getDeviceId)
    const val KEY_DEVICE_MANUFACTURER = "manufacturer"
    const val KEY_DEVICE_MODEL = "model"
    const val KEY_DEVICE_OS_VERSION = "os_version"
    const val KEY_DEVICE_SDK_VERSION = "sdk_version"
    const val KEY_DEVICE_SCREEN_DENSITY = "screen_density"
    const val KEY_DEVICE_SCREEN_RESOLUTION = "screen_resolution"

    // Default application names
    const val DEFAULT_APP_NAME_ANDROID_WEBVIEW_SDK = "android_webview_sdk"
} 
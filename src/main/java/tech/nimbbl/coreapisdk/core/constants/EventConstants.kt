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

} 
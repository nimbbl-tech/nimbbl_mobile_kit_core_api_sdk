package tech.nimbbl.coreapisdk.utils.logging

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import android.util.Log
import tech.nimbbl.coreapisdk.core.NimbblCoreApiSDK
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled

/**
 * Utility class for safe event logging across the Nimbbl SDK
 * Provides a centralized way to log events without impacting the main flow
 */
object EventLoggingUtils {
    
    private const val TAG = "EventLoggingUtils"
    
    /**
     * Safely log an event without impacting the main flow
     * @param context The context for logging
     * @param eventName The name of the event to log
     * @param orderId Optional order ID for the event
     * @param token Optional token for the event
     * @param subMerchantId Optional sub merchant ID for the event
     * @param additionalData Optional additional data for the event
     * @param sdkVersion Optional SDK version for the event
     */
    fun safeLogEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null,
        sdkVersion: String? = null
    ) {
        // Debug logging for event parameters
        if (is_debug_enabled) {
            Log.d(TAG, "=== EVENT CALL DEBUG ===")
            Log.d(TAG, "Event: $eventName")
            Log.d(TAG, "Order ID: $orderId")
            Log.d(TAG, "Sub Merchant ID: $subMerchantId")
            Log.d(TAG, "Token: ${token?.take(20)}...")
            Log.d(TAG, "Additional Data: $additionalData")
            Log.d(TAG, "SDK Version: $sdkVersion")
            Log.d(TAG, "Context: ${context.javaClass.simpleName}")
            Log.d(TAG, "=========================")
        }
        
        try {
            val customAppInfo = if (sdkVersion != null) {
                mapOf("appVersion" to sdkVersion)
            } else null
            
            NimbblCoreApiSDK.getInstance()?.logEvent(
                context,
                eventName,
                orderId,
                token,
                subMerchantId,
                additionalData,
                null, // customUserAgent
                null, // customDeviceInfo
                customAppInfo
            )
        } catch (e: Exception) {
            if (is_debug_enabled) {
                Log.w(TAG, "Event logging failed for $eventName: ${e.message}")
            }
        }
    }
    
    // Context-free version removed as NimbblCoreApiSDK.logEvent requires a non-null Context
    
    /**
     * Safely log an event with a custom tag for better debugging
     * @param context The context for logging
     * @param eventName The name of the event to log
     * @param orderId Optional order ID for the event
     * @param token Optional token for the event
     * @param subMerchantId Optional sub merchant ID for the event
     * @param additionalData Optional additional data for the event
     * @param customTag Custom tag for the log message
     * @param sdkVersion Optional SDK version for the event
     */
    fun safeLogEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null,
        customTag: String,
        sdkVersion: String? = null
    ) {
        try {
            val customAppInfo = if (sdkVersion != null) {
                mapOf("appVersion" to sdkVersion)
            } else null
            
            NimbblCoreApiSDK.getInstance()?.logEvent(
                context,
                eventName,
                orderId,
                token,
                subMerchantId,
                additionalData,
                null, // customUserAgent
                null, // customDeviceInfo
                customAppInfo
            )
        } catch (e: Exception) {
            if (is_debug_enabled) {
                Log.w(customTag, "Event logging failed for $eventName: ${e.message}")
            }
        }
    }
} 
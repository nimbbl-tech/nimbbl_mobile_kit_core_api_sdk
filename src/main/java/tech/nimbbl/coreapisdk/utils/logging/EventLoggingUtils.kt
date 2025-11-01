package tech.nimbbl.coreapisdk.utils.logging

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import android.util.Log
import tech.nimbbl.coreapisdk.core.NimbblCoreApiSDK
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.utils.DataMasker

/**
 * Utility class for safe event logging across the Nimbbl SDK
 * Provides a centralized way to log events without impacting the main flow
 */
object EventLoggingUtils {
    
    private const val TAG = "EventLoggingUtils"
    
    /**
     * Masks tokens in additional data, specifically in URL fields
     * @param additionalData The additional data map that may contain URLs with tokens
     * @return A new map with masked tokens in URL fields
     */
    private fun maskTokensInAdditionalData(additionalData: Map<String, Any>?): Map<String, Any>? {
        if (additionalData == null) return null
        
        return additionalData.mapValues { (key, value) ->
            when (value) {
                is String -> {
                    // Check if this looks like a URL field that might contain tokens
                    if (key.contains("url", ignoreCase = true) || 
                        key.contains("callback", ignoreCase = true) ||
                        key.contains("redirect", ignoreCase = true) ||
                        key.contains("intercepted", ignoreCase = true)) {
                        DataMasker.maskTokensInUrl(value)
                    } else {
                        value
                    }
                }
                else -> value
            }
        }
    }
    
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
        // Mask tokens in additional data URLs
        val maskedAdditionalData = maskTokensInAdditionalData(additionalData)
        
        // Debug logging for event parameters
        if (is_debug_enabled) {
            Log.d(TAG, "=== EVENT CALL DEBUG ===")
            Log.d(TAG, "Event: $eventName")
            Log.d(TAG, "Order ID: $orderId")
            Log.d(TAG, "Sub Merchant ID: $subMerchantId")
            Log.d(TAG, "Token: ${DataMasker.maskToken(token)}")
            Log.d(TAG, "Additional Data: $maskedAdditionalData")
            Log.d(TAG, "SDK Version: $sdkVersion")
            Log.d(TAG, "Context: ${context.javaClass.simpleName}")
            Log.d(TAG, "=========================")
        }
        
        try {
            val customAppInfo = if (!sdkVersion.isNullOrBlank()) {
                mapOf("appVersion" to sdkVersion)
            } else {
                // If SDK version is not provided, log a warning but don't fail
                if (is_debug_enabled) {
                    Log.w(TAG, "SDK version is null or empty for event '$eventName', will fallback to Core API SDK version")
                }
                null
            }
            
            NimbblCoreApiSDK.getInstance()?.logEvent(
                context,
                eventName,
                orderId,
                token,
                subMerchantId,
                maskedAdditionalData,
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
            // Mask tokens in additional data URLs
            val maskedAdditionalData = maskTokensInAdditionalData(additionalData)
            
            val customAppInfo = if (!sdkVersion.isNullOrBlank()) {
                mapOf("appVersion" to sdkVersion)
            } else {
                // If SDK version is not provided, log a warning but don't fail
                if (is_debug_enabled) {
                    Log.w(customTag, "SDK version is null or empty for event '$eventName', will fallback to Core API SDK version")
                }
                null
            }
            
            NimbblCoreApiSDK.getInstance()?.logEvent(
                context,
                eventName,
                orderId,
                token,
                subMerchantId,
                maskedAdditionalData,
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
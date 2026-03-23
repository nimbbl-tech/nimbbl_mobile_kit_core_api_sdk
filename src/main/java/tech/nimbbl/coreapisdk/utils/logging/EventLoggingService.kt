package tech.nimbbl.coreapisdk.utils.logging

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import tech.nimbbl.coreapisdk.api.HttpConnectionHelper
import tech.nimbbl.coreapisdk.core.constants.Constants
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.core.constants.EventConstants
import tech.nimbbl.coreapisdk.utils.DataMasker
import tech.nimbbl.coreapisdk.utils.extensions.getDeviceInfo
import tech.nimbbl.coreapisdk.utils.extensions.getIPAddress
import tech.nimbbl.coreapisdk.utils.extensions.md5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Event logging service for Nimbbl Core API SDK
 * Sends analytics events to Nimbbl's event logging endpoint
 */
class EventLoggingService private constructor() {
    
    companion object {
        private const val TAG = "EventLoggingService"
        
        @Volatile
        private var INSTANCE: EventLoggingService? = null
        
        // Store app code set by WebView SDK
        @Volatile
        private var appCode: String? = null
        
        // Event logging control flags
        @Volatile
        private var isEventLoggingEnabled: Boolean = true  // Always enabled by default
        
        fun getInstance(): EventLoggingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventLoggingService().also { INSTANCE = it }
            }
        }
        
        /**
         * Set the app code for logging events
         * This method is called by the WebView SDK when initializing the Core API SDK
         */
        fun setAppCode(code: String?) {
            appCode = code
        }
        
        /**
         * Enable or disable event logging to server
         * @param enabled true to enable event logging, false to disable
         */
        fun setEventLoggingEnabled(enabled: Boolean) {
            isEventLoggingEnabled = enabled
            if (is_debug_enabled) {
                Log.d(TAG, "Event logging ${if (enabled) "enabled" else "disabled"}")
            }
        }
        
        /**
         * Check if event logging is enabled
         */
        fun isEventLoggingEnabled(): Boolean = isEventLoggingEnabled
    }
    
    /**
     * Log an event to Nimbbl's event logging service
     * This method is completely non-blocking and will not impact the main flow
     * @param context Android context
     * @param eventName Name of the event
     * @param orderId Order ID (optional)
     * @param token JWT token (optional)
     * @param additionalData Additional data to include in the event (optional)
     * @param customUserAgent Custom user agent string (optional)
     * @param customDeviceInfo Custom device information (optional)
     * @param customAppInfo Custom app information (optional)
     */
    fun logEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null,
        customUserAgent: String? = null,
        customDeviceInfo: Map<String, String>? = null,
        customAppInfo: Map<String, String>? = null
    ) {
        // Debug logging for event parameters
        if (is_debug_enabled) {
            Log.d(TAG, "=== EVENT LOGGING DEBUG ===")
            Log.d(TAG, "Event Name: $eventName")
            Log.d(TAG, "Order ID: $orderId")
            Log.d(TAG, "Sub Merchant ID: $subMerchantId")
            Log.d(TAG, "Token: ${DataMasker.maskToken(token)}")
            Log.d(TAG, "Additional Data: $additionalData")
            Log.d(TAG, "Custom User Agent: $customUserAgent")
            Log.d(TAG, "Custom Device Info: $customDeviceInfo")
            Log.d(TAG, "Custom App Info: $customAppInfo")
            Log.d(TAG, "Context: ${context.javaClass.simpleName}")
            Log.d(TAG, "Event Logging Enabled: $isEventLoggingEnabled")
            Log.d(TAG, "================================")
        }
        
        // Event logging is always enabled - events are always sent to server
        // This check is kept for future flexibility but currently always passes
        if (!isEventLoggingEnabled) {
            if (is_debug_enabled) {
                Log.d(TAG, "Event logging is disabled, skipping event: $eventName")
            }
            return
        }
        
        // Use a separate coroutine scope to ensure it doesn't interfere with main flow
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val eventData = buildEventData(
                    context, 
                    eventName, 
                    orderId, 
                    token,
                    subMerchantId,
                    additionalData,
                    customUserAgent,
                    customDeviceInfo,
                    customAppInfo
                )
                
                // Debug print the final event data
                if (is_debug_enabled) {
                    Log.d(TAG, "=== FINAL EVENT DATA ===")
                    Log.d(TAG, "Event Data JSON: ${eventData.toString()}")
                    Log.d(TAG, "=========================")
                }
                
                sendEventToServer(eventData)
            } catch (e: Exception) {
                // Enhanced error handling for event logging failures
                Log.e(TAG, "Event logging failed for event '$eventName': ${e.message}", e)
                
                // Try to log a simplified event as fallback
                try {
                    val fallbackEventData = buildFallbackEventData(
                        context,
                        eventName,
                        orderId,
                        token,
                        subMerchantId,
                        e.message ?: "Unknown error"
                    )
                    sendEventToServer(fallbackEventData)
                } catch (fallbackException: Exception) {
                    Log.e(TAG, "Fallback event logging also failed: ${fallbackException.message}", fallbackException)
                }
                
                // Don't rethrow the exception to ensure main flow continues
            }
        }
    }
    
    private fun buildEventData(
        context: Context,
        eventName: String,
        orderId: String?,
        token: String?,
        subMerchantId: String?,
        additionalData: Map<String, Any>?,
        customUserAgent: String?,
        customDeviceInfo: Map<String, String>?,
        customAppInfo: Map<String, String>?
    ): JSONObject {
        val eventData = JSONObject()
        val data = JSONObject()
        
        // Basic event information
        eventData.put(EventConstants.KEY_NAME, eventName)
        val transactionId = additionalData?.get(EventConstants.KEY_TRANSACTION_ID)?.toString() ?: "unknown"
        eventData.put(EventConstants.KEY_TRANSACTION_ID, transactionId)
        eventData.put(EventConstants.KEY_ORDER_ID, orderId)
        eventData.put(EventConstants.KEY_VERSION, customAppInfo?.get(EventConstants.KEY_APP_VERSION) ?: Constants.sdk_version)
        eventData.put(EventConstants.KEY_SUBMERCHANT_ID, subMerchantId ?: "")
        eventData.put(EventConstants.KEY_CLIENT_IP, customDeviceInfo?.get(EventConstants.KEY_IP_ADDRESS) ?: getIPAddress(true).ifEmpty { "unknown" })
        eventData.put(EventConstants.KEY_PRODUCT_CODE, customAppInfo?.get(EventConstants.KEY_PRODUCT_NAME) ?: "Payments")
        eventData.put(EventConstants.KEY_APPLICATION_NAME, getAppCode(customAppInfo))

        val now = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        eventData.put(EventConstants.KEY_EVENT_TIMESTAMP, sdf.format(now))

        data.put(EventConstants.KEY_UA, customUserAgent ?: getUserAgent())
        data.put(EventConstants.KEY_UA_BROWSER, customDeviceInfo?.get(EventConstants.KEY_UA_BROWSER) ?: getBrowserInfo())
        data.put(EventConstants.KEY_UA_CPU, customDeviceInfo?.get(EventConstants.KEY_UA_CPU) ?: Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown")
        data.put(EventConstants.KEY_UA_DEVICE, customDeviceInfo?.get(EventConstants.KEY_UA_DEVICE) ?: (Build.MANUFACTURER + " " + Build.MODEL))
        data.put(EventConstants.KEY_UA_ENGINE, customDeviceInfo?.get(EventConstants.KEY_UA_ENGINE) ?: "Android WebView")
        data.put(EventConstants.KEY_UA_OS, customDeviceInfo?.get(EventConstants.KEY_UA_OS) ?: Build.VERSION.RELEASE)
        data.put(EventConstants.KEY_PRODUCT_CODE, customAppInfo?.get(EventConstants.KEY_PRODUCT_NAME) ?: "Payments")
        data.put(EventConstants.KEY_APPLICATION_NAME, getAppCode(customAppInfo))
        data.put(EventConstants.KEY_ENVIRONMENT, customAppInfo?.get(EventConstants.KEY_ENVIRONMENT) ?: "")
        data.put(EventConstants.KEY_APP_VERSION, customAppInfo?.get(EventConstants.KEY_APP_VERSION) ?: Constants.sdk_version)
        orderId?.let { data.put(EventConstants.KEY_ORDER_ID_DATA, it) }
        token?.let { data.put(EventConstants.KEY_TOKEN, DataMasker.maskToken(it)) }
        data.put(EventConstants.KEY_DEVICE_ID, customDeviceInfo?.get(EventConstants.KEY_DEVICE_ID) ?: getDeviceId(context))
        data.put(EventConstants.KEY_CARRIER, customDeviceInfo?.get(EventConstants.KEY_CARRIER) ?: getCarrierInfo(context))
        data.put(EventConstants.KEY_CARRIER_TYPE, customDeviceInfo?.get(EventConstants.KEY_CARRIER_TYPE) ?: "unknown")
        data.put(EventConstants.KEY_IP_ADDRESS, customDeviceInfo?.get(EventConstants.KEY_IP_ADDRESS) ?: getIPAddress(true).ifEmpty { "unknown" })
        data.put(EventConstants.KEY_EXPIRY_DATE, JSONObject())
        data.put(EventConstants.KEY_EVENT_TIMESTAMP_DATA, sdf.format(now))
        data.put(EventConstants.KEY_EVENT_TIMEZONE, "Asia/Calcutta")
        data.put(EventConstants.KEY_EVENT_TIMEZONE_OFFSET, "-5.5 hrs")
        val localSdf = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(India Standard Time)'", Locale.US)
        localSdf.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        data.put(EventConstants.KEY_EVENT_TIME_LOCAL, localSdf.format(now))
        val utcSdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        utcSdf.timeZone = TimeZone.getTimeZone("UTC")
        data.put(EventConstants.KEY_EVENT_TIME_UTC, utcSdf.format(now))
        
        token?.let {
            try {
                val parts = it.split(".")
                if (parts.size == 3) {
                    val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                    val payloadJson = JSONObject(payload)
                    val exp = payloadJson.optLong("exp", 0L)
                    if (exp > 0) {
                        val expDate = Date(exp * 1000)
                        data.put(EventConstants.KEY_TOKEN_EXPIRY_DATE, utcSdf.format(expDate))
                    }
                }
            } catch (e: Exception) {
                if (is_debug_enabled) Log.w(TAG, "Could not parse token expiry: ${e.message}")
            }
        }
        
        additionalData?.forEach { (key, value) ->
            when (value) {
                is String -> data.put(key, if (key.lowercase().contains("token")) DataMasker.maskToken(value) else value)
                is Int -> data.put(key, value)
                is Long -> data.put(key, value)
                is Double -> data.put(key, value)
                is Float -> data.put(key, value)
                is Boolean -> data.put(key, value)
                else -> data.put(key, value.toString())
            }
        }
        
        eventData.put(EventConstants.KEY_DATA, data)
        return eventData
    }
    
    /**
     * Build fallback event data when main event logging fails
     */
    private fun buildFallbackEventData(
        context: Context,
        eventName: String,
        orderId: String?,
        token: String?,
        subMerchantId: String?,
        errorMessage: String
    ): JSONObject {
        val eventData = JSONObject()
        val data = JSONObject()
        eventData.put(EventConstants.KEY_NAME, eventName)
        eventData.put(EventConstants.KEY_TRANSACTION_ID, "fallback")
        eventData.put(EventConstants.KEY_ORDER_ID, orderId)
        eventData.put(EventConstants.KEY_VERSION, Constants.sdk_version)
        eventData.put(EventConstants.KEY_SUBMERCHANT_ID, subMerchantId ?: "")
        eventData.put(EventConstants.KEY_CLIENT_IP, getIPAddress(true).ifEmpty { "unknown" })
        eventData.put(EventConstants.KEY_PRODUCT_CODE, "Payments")
        eventData.put(EventConstants.KEY_APPLICATION_NAME, getAppCode(null))
        val now = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        eventData.put(EventConstants.KEY_EVENT_TIMESTAMP, sdf.format(now))
        data.put(EventConstants.KEY_UA, getUserAgent())
        data.put(EventConstants.KEY_UA_DEVICE, Build.MANUFACTURER + " " + Build.MODEL)
        data.put(EventConstants.KEY_UA_OS, Build.VERSION.RELEASE)
        data.put(EventConstants.KEY_PRODUCT_CODE, "Payments")
        data.put(EventConstants.KEY_APPLICATION_NAME, getAppCode(null))
        data.put(EventConstants.KEY_APP_VERSION, Constants.sdk_version)
        data.put(EventConstants.KEY_DEVICE_ID, getDeviceId(context))
        data.put(EventConstants.KEY_IP_ADDRESS, getIPAddress(true).ifEmpty { "unknown" })
        data.put(EventConstants.KEY_LOGGING_ERROR, errorMessage)
        orderId?.let { data.put(EventConstants.KEY_ORDER_ID_DATA, it) }
        token?.let { data.put(EventConstants.KEY_TOKEN, DataMasker.maskToken(it)) }
        eventData.put(EventConstants.KEY_DATA, data)
        return eventData
    }

    private fun sendEventToServer(eventData: JSONObject) {
        try {
            val url = "${Constants.EVENT_LOG_URL}?tenantId=${Constants.DEFAULT_TENANT_ID}"
            val requestBodyJson = eventData.toString()
            val headers = mapOf("Content-Type" to "application/json")
            val response = HttpConnectionHelper.post(url, headers, requestBodyJson)
            if (is_debug_enabled) {
                ApiLoggingUtils.logResponseDetails(
                    code = response.code,
                    message = response.message,
                    body = response.body,
                    customTag = TAG
                )
            }
            if (!response.isSuccessful) {
                if (is_debug_enabled) {
                    Log.w(TAG, "Event logging failed: ${response.code} - ${response.message}")
                }
            } else {
                if (is_debug_enabled) {
                    Log.d(TAG, "Event logged successfully: ${eventData.optString(EventConstants.KEY_NAME, "unknown")}")
                }
            }
        } catch (e: Exception) {
            if (is_debug_enabled) {
                Log.e(TAG, "=== EVENT LOGGING EXCEPTION ===")
                Log.e(TAG, "Exception: ${e.javaClass.simpleName}")
                Log.e(TAG, "Message: ${e.message}")
                Log.e(TAG, "Stack Trace: ${e.stackTraceToString()}")
                Log.e(TAG, "================================")
            }
        }
    }
    
    private fun getUserAgent(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    private fun getBrowserInfo(): String {
        return Build.VERSION.RELEASE
    }
    
    private fun getDeviceId(context: Context): String {
        return try {
            // Use privacy-friendly device identification
            val deviceInfo = getDeviceInfo(context)
            val deviceCharacteristics = StringBuilder().apply {
                append(deviceInfo[EventConstants.KEY_DEVICE_MANUFACTURER] ?: "unknown")
                append("_")
                append(deviceInfo[EventConstants.KEY_DEVICE_MODEL] ?: "unknown")
                append("_")
                append(deviceInfo[EventConstants.KEY_DEVICE_OS_VERSION] ?: "unknown")
                append("_")
                append(deviceInfo[EventConstants.KEY_DEVICE_SDK_VERSION] ?: "unknown")
            }.toString()
            
            // Create a hash of the device characteristics for consistent identification
            md5(deviceCharacteristics)
        } catch (e: Exception) {
            if (is_debug_enabled) {
                Log.w(TAG, "Could not generate device ID: ${e.message}")
            }
            "unknown"
        }
    }
    
    private fun getCarrierInfo(context: Context): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.networkOperatorName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    /**
     * Get the app code from WebView SDK if available, otherwise use default or custom app info
     */
    private fun getAppCode(customAppInfo: Map<String, String>?): String {
        return try {
            // Try to get app code set by WebView SDK first
            if (!appCode.isNullOrEmpty()) {
                return appCode!!
            }
            
            // Fallback to custom app info
            customAppInfo?.get(EventConstants.KEY_APPLICATION_NAME) ?: EventConstants.DEFAULT_APP_NAME_ANDROID_WEBVIEW_SDK
        } catch (e: Exception) {
            // Fallback to default if any error occurs
            customAppInfo?.get(EventConstants.KEY_APPLICATION_NAME) ?: EventConstants.DEFAULT_APP_NAME_ANDROID_WEBVIEW_SDK
        }
    }
} 
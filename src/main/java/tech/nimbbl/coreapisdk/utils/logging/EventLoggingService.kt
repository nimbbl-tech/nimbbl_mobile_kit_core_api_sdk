package tech.nimbbl.coreapisdk.utils.logging

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tech.nimbbl.coreapisdk.core.constants.Constants
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.utils.DataMasker
import tech.nimbbl.coreapisdk.utils.extensions.getDeviceInfo
import tech.nimbbl.coreapisdk.utils.extensions.md5
import java.net.NetworkInterface
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
        
        fun getInstance(): EventLoggingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventLoggingService().also { INSTANCE = it }
            }
        }
    }
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)  // Reduced timeout
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)    // Reduced timeout
        .writeTimeout(5, java.util.concurrent.TimeUnit.SECONDS)   // Added write timeout
        .build()
    
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
            Log.d(TAG, "================================")
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
                    Log.d(TAG, "Event Data JSON: ${Gson().toJson(eventData)}")
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
    ): JsonObject {
        val eventData = JsonObject()
        val data = JsonObject()
        
        // Basic event information
        eventData.addProperty("name", eventName)
        // Get transaction ID from additional data, fallback to orderId if not available
        val transactionId = additionalData?.get("transactionId")?.toString() ?: "unknown"
        eventData.addProperty("transactionId", transactionId)
        eventData.addProperty("orderId", orderId)
        eventData.addProperty("version", customAppInfo?.get("appVersion") ?: Constants.sdk_version)
        eventData.addProperty("submerchantId", subMerchantId ?: "")
        eventData.addProperty("clientIP", customDeviceInfo?.get("ip_address") ?: getIPAddress())
        eventData.addProperty("productCode", customAppInfo?.get("product_name") ?: "Payments")
        eventData.addProperty("appCode", customAppInfo?.get("application_name") ?: "android_webview_sdk")

        // Timestamp information
        val now = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        eventData.addProperty("eventTimestamp", sdf.format(now))

        // User agent information
        data.addProperty("ua", customUserAgent ?: getUserAgent())
        data.addProperty("ua_browser", customDeviceInfo?.get("ua_browser") ?: getBrowserInfo())
        data.addProperty("ua_cpu", customDeviceInfo?.get("ua_cpu") ?:Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown")
        data.addProperty("ua_device", customDeviceInfo?.get("ua_device") ?: (Build.MANUFACTURER + " " + Build.MODEL))
        data.addProperty("ua_engine", customDeviceInfo?.get("ua_engine") ?: "Android WebView")
        data.addProperty("ua_os", customDeviceInfo?.get("ua_os") ?: Build.VERSION.RELEASE)
        
        // Application information
        data.addProperty("productCode", customAppInfo?.get("product_name") ?: "Payments")
        data.addProperty("appCode", customAppInfo?.get("application_name") ?: "android_webview_sdk")
        data.addProperty("environment", customAppInfo?.get("environment") ?: "")
                    data.addProperty("appVersion", customAppInfo?.get("appVersion") ?: Constants.sdk_version)
        
        // Order and token information
        orderId?.let { data.addProperty("order_id", it) }
        token?.let { data.addProperty("token", DataMasker.maskToken(it)) }

        // Device information
        data.addProperty("device_id", customDeviceInfo?.get("device_id") ?: getDeviceId(context))
        data.addProperty("carrier", customDeviceInfo?.get("carrier") ?: getCarrierInfo(context))
        data.addProperty("carrier_type", customDeviceInfo?.get("carrier_type") ?: "unknown")
        data.addProperty("ip_address", customDeviceInfo?.get("ip_address") ?: getIPAddress())
        
        // Token expiry information
        val expiryDate = JsonObject()
        data.add("expiryDate", expiryDate)
        
        // Timestamp information
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        
        data.addProperty("event_timestamp", sdf.format(now))
        data.addProperty("event_timezone", "Asia/Calcutta")
        data.addProperty("event_timezone_offset", "-5.5 hrs")
        
        val localSdf = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(India Standard Time)'", Locale.US)
        localSdf.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        data.addProperty("event_time_local", localSdf.format(now))
        
        val utcSdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        utcSdf.timeZone = TimeZone.getTimeZone("UTC")
        data.addProperty("event_time_utc", utcSdf.format(now))
        
        // Token expiry date (if token provided)
        token?.let {
            // Parse JWT token to get expiry
            try {
                val parts = it.split(".")
                if (parts.size == 3) {
                    val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                    val payloadJson = Gson().fromJson(payload, JsonObject::class.java)
                    val exp = payloadJson.get("exp")?.asLong ?: 0L
                    if (exp > 0) {
                        val expDate = Date(exp * 1000)
                        data.addProperty("token_expiry_date", utcSdf.format(expDate))
                    } else {
                        // No expiry date found
                    }
                } else {
                    // Invalid token format
                }
            } catch (e: Exception) {
                if (is_debug_enabled) {
                    Log.w(TAG, "Could not parse token expiry: ${e.message}")
                }
            }
        }
        
        // Add any additional data (with token masking)
        additionalData?.forEach { (key, value) ->
            when (value) {
                is String -> {
                    // Mask tokens in additional data
                    if (key.lowercase().contains("token")) {
                        data.addProperty(key, DataMasker.maskToken(value))
                    } else {
                        data.addProperty(key, value)
                    }
                }
                is Int -> data.addProperty(key, value)
                is Long -> data.addProperty(key, value)
                is Double -> data.addProperty(key, value)
                is Float -> data.addProperty(key, value)
                is Boolean -> data.addProperty(key, value)
                else -> data.addProperty(key, value.toString())
            }
        }
        
        eventData.add("data", data)
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
    ): JsonObject {
        val eventData = JsonObject()
        val data = JsonObject()
        
        // Basic event information
        eventData.addProperty("name", eventName)
        eventData.addProperty("transactionId", "fallback")
        eventData.addProperty("orderId", orderId)
        eventData.addProperty("version", Constants.sdk_version)
        eventData.addProperty("submerchantId", subMerchantId ?: "")
        eventData.addProperty("clientIP", getIPAddress())
        eventData.addProperty("productCode", "Payments")
        eventData.addProperty("appCode",  "android_webview_sdk")

        // Timestamp information
        val now = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        eventData.addProperty("eventTimestamp", sdf.format(now))

        // Minimal data for fallback
        data.addProperty("ua", getUserAgent())
        data.addProperty("ua_device", Build.MANUFACTURER + " " + Build.MODEL)
        data.addProperty("ua_os", Build.VERSION.RELEASE)
        data.addProperty("productCode", "Payments")
        data.addProperty("appCode", "android_webview_sdk")
        data.addProperty("appVersion", Constants.sdk_version)
        data.addProperty("device_id", getDeviceId(context))
        data.addProperty("ip_address", getIPAddress())
        data.addProperty("logging_error", errorMessage)
        
        // Order and token information
        orderId?.let { data.addProperty("order_id", it) }
        token?.let { data.addProperty("token", DataMasker.maskToken(it)) }

        eventData.add("data", data)
        return eventData
    }

    private fun sendEventToServer(eventData: JsonObject) {
        try {
            val requestBody = Gson().toJson(eventData)
                .toRequestBody("application/json".toMediaTypeOrNull())
            
            // Debug print server request details
            if (is_debug_enabled) {
                Log.d(TAG, "=== SERVER REQUEST DEBUG ===")
                Log.d(TAG, "URL: ${Constants.EVENT_LOG_URL}?tenantId=${Constants.DEFAULT_TENANT_ID}")
                Log.d(TAG, "Request Body: $requestBody")
                Log.d(TAG, "=============================")
            }
            
            val request = Request.Builder()
                .url("${Constants.EVENT_LOG_URL}?tenantId=${Constants.DEFAULT_TENANT_ID}")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                // Debug print server response
                if (is_debug_enabled) {
                    Log.d(TAG, "=== SERVER RESPONSE DEBUG ===")
                    Log.d(TAG, "Response Code: ${response.code}")
                    Log.d(TAG, "Response Message: ${response.message}")
                    Log.d(TAG, "Response Headers: ${response.headers}")
                }

                val responseBody = response.body?.string()
                if (is_debug_enabled) {
                    Log.d(TAG, "Response Body: $responseBody")
                    Log.d(TAG, "==============================")
                }
                
                if (!response.isSuccessful) {
                    if (is_debug_enabled) {
                        Log.w(TAG, "Event logging failed: ${response.code} - ${response.message}")
                    }
                } else {
                    if (is_debug_enabled) {
                        Log.d(TAG, "Event logged successfully: ${eventData.get("name")?.asString ?: "unknown"}")
                    }
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
        } catch (e: java.net.UnknownHostException) {
            if (is_debug_enabled) {
                Log.w(TAG, "Event logging network error: ${e.message}")
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
                append(deviceInfo["manufacturer"] ?: "unknown")
                append("_")
                append(deviceInfo["model"] ?: "unknown")
                append("_")
                append(deviceInfo["os_version"] ?: "unknown")
                append("_")
                append(deviceInfo["sdk_version"] ?: "unknown")
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
    
    private fun getIPAddress(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && (address.hostAddress?.indexOf(':') ?: -1) < 0) {
                        return address.hostAddress ?: "unknown"
                    } else {
                        // Continue to next address
                    }
                }
            }
            "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
} 
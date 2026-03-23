package tech.nimbbl.coreapisdk.utils.logging

import android.util.Log
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.utils.DataMasker

/**
 * Centralized API logging utility for the Nimbbl Android SDK
 * Provides consistent logging for all API requests and responses
 */
object ApiLoggingUtils {
    
    private const val TAG = "NimbblApiLogger"
    private const val REQUEST_PREFIX = "=== API REQUEST ==="
    private const val RESPONSE_PREFIX = "=== API RESPONSE ==="
    private const val SEPARATOR = "========================="
    
    /**
     * Log API request with custom details
     * @param method HTTP method
     * @param url Request URL
     * @param headers Request headers
     * @param body Request body (optional)
     * @param customTag Optional custom tag for the log (defaults to TAG)
     */
    fun logRequestDetails(
        method: String,
        url: String,
        headers: String? = null,
        body: String? = null,
        customTag: String? = null
    ) {
        if (!is_debug_enabled) return
        
        val tag = customTag ?: TAG
        Log.d(tag, REQUEST_PREFIX)
        Log.d(tag, "Method: $method")
        Log.d(tag, "URL: ${DataMasker.maskTokensInUrl(url)}")
        if (headers != null) {
            Log.d(tag, "Headers: ${DataMasker.sanitizeForLogging(headers)}")
        }
        if (body != null) {
            Log.d(tag, "Request Body: ${DataMasker.sanitizeForLogging(body)}")
        }
        Log.d(tag, SEPARATOR)
    }
    
    /**
     * Log API response with custom details
     * @param code Response code
     * @param message Response message
     * @param headers Response headers
     * @param body Response body (optional)
     * @param customTag Optional custom tag for the log (defaults to TAG)
     */
    fun logResponseDetails(
        code: Int,
        message: String,
        headers: String? = null,
        body: String? = null,
        customTag: String? = null
    ) {
        if (!is_debug_enabled) return
        
        val tag = customTag ?: TAG
        Log.d(tag, RESPONSE_PREFIX)
        Log.d(tag, "Response Code: $code")
        Log.d(tag, "Response Message: $message")
        if (headers != null) {
            Log.d(tag, "Response Headers: ${DataMasker.sanitizeForLogging(headers)}")
        }
        if (body != null) {
            Log.d(tag, "Response Body: ${DataMasker.sanitizeForLogging(body)}")
        }
        Log.d(tag, SEPARATOR)
    }
    
    /**
     * Log a simple API call with minimal details
     * @param method HTTP method
     * @param url Request URL
     * @param responseCode Response code
     * @param customTag Optional custom tag for the log (defaults to TAG)
     */
    fun logSimpleApiCall(
        method: String,
        url: String,
        responseCode: Int,
        customTag: String? = null
    ) {
        if (!is_debug_enabled) return
        
        val tag = customTag ?: TAG
        Log.d(tag, "$method $url -> $responseCode")
    }
}

package tech.nimbbl.coreapisdk.utils.logging

import android.util.Log
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled

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
     * Log API request details
     * @param request The OkHttp request object
     * @param customTag Optional custom tag for the log (defaults to TAG)
     */
    fun logRequest(request: Request, customTag: String? = null) {
        if (!is_debug_enabled) return
        
        val tag = customTag ?: TAG
        Log.d(tag, REQUEST_PREFIX)
        Log.d(tag, "Method: ${request.method}")
        Log.d(tag, "URL: ${request.url}")
        Log.d(tag, "Headers: ${request.headers}")
        
        // Log request body if present
        request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                Log.d(tag, "Request Body: ${buffer.readUtf8()}")
            } catch (e: Exception) {
                Log.d(tag, "Request Body: [Error reading body: ${e.message}]")
            }
        }
        Log.d(tag, SEPARATOR)
    }
    
    /**
     * Log API response details
     * @param response The OkHttp response object
     * @param customTag Optional custom tag for the log (defaults to TAG)
     */
    fun logResponse(response: Response, customTag: String? = null) {
        if (!is_debug_enabled) return
        
        val tag = customTag ?: TAG
        Log.d(tag, RESPONSE_PREFIX)
        Log.d(tag, "Response Code: ${response.code}")
        Log.d(tag, "Response Message: ${response.message}")
        Log.d(tag, "Response Headers: ${response.headers}")
        
        // Log response body
        val responseBody = response.body
        if (responseBody != null) {
            try {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body
                val buffer = source.buffer
                val bodyString = buffer.clone().readUtf8()
                Log.d(tag, "Response Body: $bodyString")
            } catch (e: Exception) {
                Log.d(tag, "Response Body: [Error reading body: ${e.message}]")
            }
        }
        Log.d(tag, SEPARATOR)
    }
    
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
        Log.d(tag, "URL: $url")
        if (headers != null) {
            Log.d(tag, "Headers: $headers")
        }
        if (body != null) {
            Log.d(tag, "Request Body: $body")
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
            Log.d(tag, "Response Headers: $headers")
        }
        if (body != null) {
            Log.d(tag, "Response Body: $body")
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

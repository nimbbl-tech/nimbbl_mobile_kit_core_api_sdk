package tech.nimbbl.coreapisdk.utils.extensions

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import com.auth0.android.jwt.JWT
import tech.nimbbl.coreapisdk.utils.logging.EventLoggingUtils
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

/**
 * Utility extension functions for the Nimbbl SDK
 * These functions are shared across different SDK modules
 */

fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun getDeviceFingerPrint(): String {
    return Build.FINGERPRINT
}

/**
 * Get a privacy-friendly device identifier
 * Uses a combination of non-sensitive device characteristics
 * instead of persistent device IDs
 */
fun getDeviceID(): String {
    return try {
        // Use a combination of non-sensitive device characteristics
        val deviceCharacteristics = StringBuilder().apply {
            append(Build.MANUFACTURER ?: "unknown")
            append("_")
            append(Build.MODEL ?: "unknown")
            append("_")
            append(Build.VERSION.RELEASE ?: "unknown")
            append("_")
            append(Build.VERSION.SDK_INT.toString())
        }.toString()
        
        // Create a hash of the device characteristics
        md5(deviceCharacteristics)
    } catch (e: Exception) {
        // Fallback to a random UUID if device characteristics are not available
        UUID.randomUUID().toString()
    }
}

/**
 * Get a session-based identifier that changes on app restart
 * More privacy-friendly than persistent device IDs
 */
fun getSessionID(): String {
    return try {
        // Use app-specific characteristics for session identification
        val sessionCharacteristics = StringBuilder().apply {
            append(Build.VERSION.RELEASE ?: "unknown")
            append("_")
            append(System.currentTimeMillis() / (1000 * 60 * 60)) // Hour-based timestamp
        }.toString()
        
        md5(sessionCharacteristics)
    } catch (e: Exception) {
        UUID.randomUUID().toString()
    }
}

/**
 * Get device information for analytics (privacy-friendly)
 */
fun getDeviceInfo(context: Context): Map<String, String> {
    return mapOf(
        "manufacturer" to (Build.MANUFACTURER ?: "unknown"),
        "model" to (Build.MODEL ?: "unknown"),
        "os_version" to (Build.VERSION.RELEASE ?: "unknown"),
        "sdk_version" to Build.VERSION.SDK_INT.toString(),
        "screen_density" to context.resources.displayMetrics.densityDpi.toString(),
        "screen_resolution" to "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}"
    )
}

fun isNetConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Use modern API for Android 6.0+ (API 23+)
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        // Use legacy API for Android 5.0-5.1 (API 21-22)
        @Suppress("DEPRECATION")
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        activeNetworkInfo?.isConnected == true
    }
}

fun encodeToBase64(image: Bitmap, compressFormat: Bitmap.CompressFormat, quality: Int): String {
    val byteArrayOS = ByteArrayOutputStream()
    image.compress(compressFormat, quality, byteArrayOS)
    return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
}

fun parseJwtToken(context: Context,token: String): String {
    try {
        // Decode the JWT token
        val jwt = JWT(token)

        // Get the claim from the token (replace "claim_name" with your actual claim name)
        return jwt.getClaim("order_id").asString().toString()

    } catch (exception: Exception) {
        // Log exception event
        EventLoggingUtils.safeLogEvent(
            context, // context not available in this utility function
            "exception_thrown",
            null, // orderId not available
            null, // token not available
            null, // subMerchantId not available
            mapOf<String, Any>(
                "error" to (exception.message ?: "parseJwtToken"),
                "location" to "parseJwtToken",
                "token_length" to token.length.toString()
            )
        )
        // Handle invalid or expired JWT tokens
        return ""
    }
}
fun getMerchantIDFromJwtToken(context: Context,token: String): String {
    try {
        // Decode the JWT token
        val jwt = JWT(token)

        // Get the claim from the token
        val subMerchantId = jwt.getClaim("sub_merchant_id").asString()
        
        // Return the submerchant_id if it exists and is not null/empty, otherwise return "unknown"
        return if (subMerchantId != null && subMerchantId.isNotEmpty()) {
            subMerchantId
        } else {
            "unknown"
        }

    } catch (exception: Exception) {
        // Log exception event
        EventLoggingUtils.safeLogEvent(
            context,
            "exception_thrown",
            null, // orderId not available
            null, // token not available
            null, // subMerchantId not available
            mapOf<String, Any>(
                "error" to (exception.message ?: "Unknown error"),
                "location" to "getMerchantIDFromJwtToken",
                "token_length" to token.length.toString()
            )
        )
        // Handle invalid or expired JWT tokens
        return "unknown"
    }
}
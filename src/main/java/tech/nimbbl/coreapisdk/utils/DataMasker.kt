package tech.nimbbl.coreapisdk.utils

import android.util.Log
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled

/**
 * Data masking utility for the Nimbbl Android Core API SDK
 * Provides secure data masking for logging, analytics, and debugging:
 * auth tokens and user PII (email, phone, OTP, name, address).
 */
object DataMasker {

    // MARK: - Constants

    /// Masking character used to hide sensitive parts
    private const val MASKING_CHARACTER = "*"

    /// Minimum token length to apply masking
    private const val MINIMUM_TOKEN_LENGTH = 12

    /// Number of characters to show at the beginning of token
    private const val TOKEN_PREFIX_LENGTH = 5

    /// Number of characters to show at the end of token
    private const val TOKEN_SUFFIX_LENGTH = 7

    private const val TAG = "DataMasker"

    /// JSON keys never shown, even partially (e.g. OTP codes)
    private val FULLY_MASKED_KEYS = setOf("otp")

    /// JSON keys masked keeping only the first letter of the local part + domain
    private val EMAIL_KEYS = setOf("email")

    /// JSON keys masked keeping only the last 2 digits (phone numbers)
    private val PHONE_KEYS = setOf("mobile_number", "mobile", "phone", "phone_number")

    /// JSON keys masked keeping only the first character (names, free-text address fields)
    private val NAME_AND_ADDRESS_KEYS = setOf(
        "first_name", "last_name", "name", "displayName", "user_name",
        "address_1", "address_id", "address_type", "area", "city",
        "landmark", "pincode", "state", "street"
    )
    
    // MARK: - Public Methods
    
    /**
     * Masks a token for secure logging and analytics
     * Shows first 5 and last 7 characters, masks the middle with exactly 10 asterisks
     * @param token The token to mask
     * @return Masked token string, or original token if too short
     */
    fun maskToken(token: String?): String {
        if (token.isNullOrEmpty()) {
            return "null"
        }
        
        // If token is too short, mask entirely (never expose raw)
        if (token.length < MINIMUM_TOKEN_LENGTH) {
            return "***"
        }
        
        // If token is exactly the minimum length, show first and last characters
        if (token.length == MINIMUM_TOKEN_LENGTH) {
            val firstChar = token.take(1)
            val lastChar = token.takeLast(1)
            val middleChars = MASKING_CHARACTER.repeat(10)
            return "$firstChar$middleChars$lastChar"
        }
        
        // For longer tokens, show first 5 and last 7 characters with exactly 10 asterisks in between
        val prefix = token.take(TOKEN_PREFIX_LENGTH)
        val suffix = token.takeLast(TOKEN_SUFFIX_LENGTH)
        val middleMask = MASKING_CHARACTER.repeat(10)
        
        return "$prefix$middleMask$suffix"
    }
    
    /**
     * Masks tokens in URLs for secure logging and analytics
     * Finds token parameters in URLs and masks them while preserving the URL structure
     * @param url The URL that may contain tokens
     * @return URL with masked tokens, or original URL if no tokens found
     */
    fun maskTokensInUrl(url: String?): String {
        if (url.isNullOrEmpty()) {
            return "null"
        }
        
        try {
            // Pattern to match token parameters in URLs
            // Matches: ?token=... or &token=... followed by JWT-like tokens
            val tokenPattern = Regex("([?&]token=)([A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+){2})")
            
            return tokenPattern.replace(url) { matchResult ->
                val prefix = matchResult.groupValues[1] // "?token=" or "&token="
                val token = matchResult.groupValues[2] // The actual token
                val maskedToken = maskToken(token)
                "$prefix$maskedToken"
            }
        } catch (e: Exception) {
            if (is_debug_enabled) {
                Log.w(TAG, "Error masking tokens in URL: ${e.message}")
            }
            return url // Return original URL if masking fails
        }
    }

    /**
     * Sanitizes request/response body for safe logging.
     * Masks tokens and user PII (email, phone, OTP, name, address fields),
     * and truncates long bodies.
     */
    fun sanitizeForLogging(body: String?): String? {
        if (body.isNullOrEmpty()) return body
        return try {
            var sanitized = body
            // Mask JWT-like tokens in JSON (e.g. "token":"eyJ...")
            sanitized = maskJsonStringFields(sanitized, setOf("token", "access_token", "accessToken")) { maskToken(it) }
            // Mask Bearer tokens
            val bearerPattern = Regex("(Bearer\\s+)([A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+){2})")
            sanitized = bearerPattern.replace(sanitized) { match ->
                "${match.groupValues[1]}${maskToken(match.groupValues[2])}"
            }
            // Mask PII fields
            sanitized = maskJsonStringFields(sanitized, FULLY_MASKED_KEYS) { "***" }
            sanitized = maskJsonStringFields(sanitized, EMAIL_KEYS) { maskEmail(it) }
            sanitized = maskJsonStringFields(sanitized, PHONE_KEYS) { maskKeepingLast(it, 2) }
            sanitized = maskJsonStringFields(sanitized, NAME_AND_ADDRESS_KEYS) { maskKeepingFirst(it, 1) }
            if (sanitized.length > 500) sanitized.take(500) + "..." else sanitized
        } catch (e: Exception) {
            if (is_debug_enabled) Log.w(TAG, "sanitizeForLogging failed: ${e.message}")
            "[truncated]"
        }
    }

    // MARK: - Private helpers

    /**
     * Replaces the string value of every `"key": "value"` pair whose key is in [keys]
     * with `mask(value)`, leaving empty values untouched.
     */
    private fun maskJsonStringFields(json: String, keys: Set<String>, mask: (String) -> String): String {
        if (keys.isEmpty()) return json
        val keyPattern = keys.joinToString("|") { Regex.escape(it) }
        val regex = Regex("(\"(?:$keyPattern)\"\\s*:\\s*)\"([^\"]*)\"")
        return regex.replace(json) { match ->
            val value = match.groupValues[2]
            if (value.isEmpty()) match.value else "${match.groupValues[1]}\"${mask(value)}\""
        }
    }

    /** Masks all but the first [keep] characters, e.g. "John" -> "J***". */
    private fun maskKeepingFirst(value: String, keep: Int): String {
        if (value.length <= keep) return MASKING_CHARACTER.repeat(value.length)
        return value.take(keep) + MASKING_CHARACTER.repeat(3)
    }

    /** Masks all but the last [keep] characters, e.g. "9876543210" -> "********10". */
    private fun maskKeepingLast(value: String, keep: Int): String {
        if (value.length <= keep) return MASKING_CHARACTER.repeat(value.length)
        return MASKING_CHARACTER.repeat(value.length - keep) + value.takeLast(keep)
    }

    /** Masks the local part of an email, keeping the first character and the domain, e.g. "jane@x.com" -> "j***@x.com". */
    private fun maskEmail(value: String): String {
        val atIndex = value.indexOf('@')
        if (atIndex <= 0) return maskKeepingFirst(value, 1)
        val localPart = value.substring(0, atIndex)
        val domainPart = value.substring(atIndex)
        return maskKeepingFirst(localPart, 1) + domainPart
    }
}

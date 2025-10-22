package tech.nimbbl.coreapisdk.utils

import android.util.Log
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled

/**
 * Data masking utility for the Nimbbl Android Core API SDK
 * Provides secure data masking for logging, analytics, and debugging
 * Currently focused on token masking, can be extended for other data types in the future
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
        
        // If token is too short, return as is (but log a warning)
        if (token.length < MINIMUM_TOKEN_LENGTH) {
            if (is_debug_enabled) {
                Log.w(TAG, "Token too short for masking: ${token.length} characters")
            }
            return token
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


}

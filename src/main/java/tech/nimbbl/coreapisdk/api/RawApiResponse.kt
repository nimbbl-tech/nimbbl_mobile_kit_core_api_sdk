package tech.nimbbl.coreapisdk.api

import org.json.JSONObject

/**
 * Raw HTTP response from HttpURLConnection - holds code, message, and parsed JSON body.
 */
data class RawApiResponse(
    val code: Int,
    val message: String,
    val body: JSONObject?,
    val rawBodyString: String? = null
) {
    val isSuccessful: Boolean get() = code in 200..299
}

package tech.nimbbl.coreapisdk.api.services

import android.util.Log
import org.json.JSONObject
import tech.nimbbl.coreapisdk.api.HttpConnectionHelper
import tech.nimbbl.coreapisdk.api.HttpConnectionHelper.HttpResponse
import tech.nimbbl.coreapisdk.api.RawApiResponse
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_fingerPrint
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_ipAddress
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_userAgent

/**
 * Core API Web Service using java.net.HttpURLConnection (no OkHttp dependency).
 */
class CoreAppWebService private constructor(
    private val defaultHeaders: Map<String, String>
) {

    companion object {
        private const val TAG = "CoreAppWebService"

        // HTTP Header Keys
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val HEADER_NIMBBL_KEY = "x-nimbbl-key"
        private const val HEADER_NIMBBL_USER_TOKEN = "x-nimbbl-user-token"

        operator fun invoke(
            baseUrl: String,
            userAgent: String,
            md5: String,
            ipAddress: String
        ): CoreAppWebService? {
            return try {
                val headers = mapOf(
                    key_userAgent to userAgent,
                    key_ipAddress to ipAddress,
                    key_fingerPrint to md5
                )
                CoreAppWebService(headers)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating CoreAppWebService: ${e.message}", e)
                null
            }
        }
    }

    private fun allHeaders(extra: Map<String, String>): Map<String, String> =
        defaultHeaders + extra

    private fun parseResponse(http: HttpResponse): RawApiResponse {
        val body = http.body
        val code = http.code
        val message = http.message
        val jsonBody = if (http.isSuccessful && !body.isNullOrEmpty()) {
            try {
                JSONObject(body)
            } catch (e: Exception) {
                JSONObject().apply { put("raw", body) }
            }
        } else {
            JSONObject().apply {
                put("success", false)
                put("status", code)
                put("error", body ?: "Unknown error")
            }
        }
        return RawApiResponse(code = code, message = message, body = jsonBody, rawBodyString = body)
    }

    suspend fun cancelCheckout(url: String, auth: String, body: String): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(url, allHeaders(mapOf(HEADER_AUTHORIZATION to auth)), body)
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "cancelCheckout error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun updateOrder(url: String, auth: String, body: String): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.patch(url, allHeaders(mapOf(HEADER_AUTHORIZATION to auth)), body)
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "updateOrder error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun checkOutResource(url: String, nimbblKey: String, auth: String): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.get(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth))
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "checkOutResource error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun downloadBankLogo(fileUrl: String): ByteArray? {
        return try {
            HttpConnectionHelper.downloadBytes(fileUrl, defaultHeaders)
        } catch (e: Exception) {
            Log.e(TAG, "downloadBankLogo error: ${e.message}", e)
            null
        }
    }

    suspend fun getOrderDetails(url: String, auth: String): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.get(url, allHeaders(mapOf(HEADER_AUTHORIZATION to auth)))
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getOrderDetails error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun resolveUser(
        url: String,
        nimbblKey: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "resolveUser error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun verifyUser(
        url: String,
        nimbblKey: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "verifyUser error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun getPaymentModes(
        url: String,
        nimbblKey: String,
        auth: String,
        nimbblUserToken: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(
                    HEADER_NIMBBL_KEY to nimbblKey,
                    HEADER_AUTHORIZATION to auth,
                    HEADER_NIMBBL_USER_TOKEN to nimbblUserToken
                )),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getPaymentModes error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun getListOfBanks(
        url: String,
        nimbblKey: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getListOfBanks error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun getListOfWallets(
        url: String,
        nimbblKey: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getListOfWallets error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun initiatePayment(
        url: String,
        nimbblKey: String,
        auth: String,
        nimbblUserToken: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(
                    HEADER_NIMBBL_KEY to nimbblKey,
                    HEADER_AUTHORIZATION to auth,
                    HEADER_NIMBBL_USER_TOKEN to nimbblUserToken
                )),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "initiatePayment error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun makePayment(
        url: String,
        nimbblKey: String,
        auth: String,
        nimbblUserToken: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(
                    HEADER_NIMBBL_KEY to nimbblKey,
                    HEADER_AUTHORIZATION to auth,
                    HEADER_NIMBBL_USER_TOKEN to nimbblUserToken
                )),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "makePayment error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun getTransactionEnquiry(
        url: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getTransactionEnquiry error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun getBinData(
        url: String,
        nimbblKey: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getBinData error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun getPublicKey(url: String): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.get(url)
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "getPublicKey error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun resendOtp(
        url: String,
        nimbblKey: String,
        auth: String,
        inputPayload: String
    ): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.post(
                url,
                allHeaders(mapOf(HEADER_NIMBBL_KEY to nimbblKey, HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "resendOtp error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }

    suspend fun updateTransaction(url: String, auth: String, inputPayload: String): RawApiResponse {
        return try {
            val http = HttpConnectionHelper.put(
                url,
                allHeaders(mapOf(HEADER_AUTHORIZATION to auth)),
                inputPayload
            )
            parseResponse(http)
        } catch (e: Exception) {
            Log.e(TAG, "updateTransaction error: ${e.message}", e)
            RawApiResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        }
    }
}

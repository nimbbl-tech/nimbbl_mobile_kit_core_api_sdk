package tech.nimbbl.coreapisdk.api

/**
 * Result wrapper for API calls (used with HttpURLConnection-based API layer).
 * Holds parsed data, HTTP status, and error info.
 */
data class ApiResult<T>(
    val data: T?,
    val code: Int,
    val message: String,
    val rawBody: String? = null
) {
    val isSuccessful: Boolean get() = code in 200..299
    val isError: Boolean get() = !isSuccessful

    companion object {
        fun <T> success(data: T, code: Int = 200, message: String = "OK"): ApiResult<T> =
            ApiResult(data = data, code = code, message = message)

        fun <T> error(code: Int, message: String, rawBody: String? = null): ApiResult<T> =
            ApiResult(data = null, code = code, message = message, rawBody = rawBody)

        fun <T> fromNullable(data: T?, code: Int, message: String, rawBody: String? = null): ApiResult<T> =
            ApiResult(data = data, code = code, message = message, rawBody = rawBody)
    }
}

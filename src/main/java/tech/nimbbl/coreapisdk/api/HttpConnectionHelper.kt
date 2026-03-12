package tech.nimbbl.coreapisdk.api

import android.util.Log
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.utils.logging.ApiLoggingUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * HTTP client using built-in java.net.HttpURLConnection (no OkHttp dependency).
 */
object HttpConnectionHelper {

    private const val CONNECT_TIMEOUT_MS = 30_000
    private const val READ_TIMEOUT_MS = 30_000

    private const val METHOD_GET = "GET"
    private const val METHOD_POST = "POST"
    private const val METHOD_PUT = "PUT"
    private const val METHOD_PATCH = "PATCH"

    data class HttpResponse(
        val code: Int,
        val message: String,
        val body: String?,
        val bodyBytes: ByteArray? = null
    ) {
        val isSuccessful: Boolean get() = code in 200..299
    }

    fun request(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ): HttpResponse {
        var connection: HttpURLConnection? = null
        return try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = method
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
                doInput = true
            }
            when (method) {
                METHOD_POST, METHOD_PUT, METHOD_PATCH -> {
                    connection.doOutput = true
                    body?.let { json ->
                        connection.setRequestProperty("Content-Length", json.byteArraySize().toString())
                        connection.outputStream.use { os: OutputStream ->
                            os.write(json.toByteArray(Charsets.UTF_8))
                            os.flush()
                        }
                    } ?: run {
                        connection.setRequestProperty("Content-Length", "0")
                    }
                }
                else -> connection.connect()
            }
            val code = connection.responseCode
            val message = connection.responseMessage ?: ""
            val bodyStr = readResponseBody(connection)
            if (is_debug_enabled) {
                ApiLoggingUtils.logRequestDetails(method = method, url = url, body = body, customTag = "HttpConnection")
                ApiLoggingUtils.logResponseDetails(code = code, message = message, body = bodyStr, customTag = "HttpConnection")
            }
            HttpResponse(code = code, message = message, body = bodyStr)
        } catch (e: Exception) {
            Log.e("HttpConnectionHelper", "Request error: ${e.message}", e)
            HttpResponse(code = -1, message = e.message ?: "Unknown error", body = null)
        } finally {
            connection?.disconnect()
        }
    }

    fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
        request(METHOD_GET, url, headers, null)

    fun post(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): HttpResponse =
        request(METHOD_POST, url, headers, body ?: "{}")

    fun put(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): HttpResponse =
        request(METHOD_PUT, url, headers, body ?: "{}")

    fun patch(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): HttpResponse =
        request(METHOD_PATCH, url, headers, body ?: "{}")

    /**
     * Download URL to byte array (e.g. for images).
     */
    fun downloadBytes(url: String, headers: Map<String, String> = emptyMap()): ByteArray? {
        var connection: HttpURLConnection? = null
        return try {
            val urlObj = URL(url)
            connection = (urlObj.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = METHOD_GET
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
                doInput = true
                connect()
            }
            if (connection.responseCode in 200..299) {
                connection.inputStream?.readBytes()
            } else null
        } catch (e: Exception) {
            Log.e("HttpConnectionHelper", "Download error: ${e.message}", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun readResponseBody(connection: HttpURLConnection): String? {
        return try {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.let { s ->
                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun String.byteArraySize(): Int = toByteArray(Charsets.UTF_8).size
}

package tech.nimbbl.coreapisdk.utils.logging

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ApiLoggingUtils only logs when Constants.is_debug_enabled (== BuildConfig.DEBUG) is true, which
 * it is for this debug test variant — so these calls exercise the real Log.d + DataMasker path,
 * not just the early-return guard. There's no Logcat-capturing assertion here (androidx.test has
 * no first-party hook for that), so this covers "every branch runs without crashing", not log
 * content — DataMasker's own masking behavior is already asserted in DataMaskerTest.
 */
@RunWith(AndroidJUnit4::class)
class ApiLoggingUtilsTest {

    @Test
    fun logRequestDetails_withHeadersAndBody_doesNotCrash() {
        ApiLoggingUtils.logRequestDetails(
            method = "POST",
            url = "https://api.test/order?token=eyJabcdefghijklmnop.body.sig",
            headers = """{"Authorization":"Bearer secret_token_value_123456"}""",
            body = """{"email":"jane@example.com","mobile_number":"9876543210"}"""
        )
    }

    @Test
    fun logRequestDetails_withoutHeadersOrBody_doesNotCrash() {
        ApiLoggingUtils.logRequestDetails(method = "GET", url = "https://api.test/order")
    }

    @Test
    fun logRequestDetails_withCustomTag_doesNotCrash() {
        ApiLoggingUtils.logRequestDetails(
            method = "GET", url = "https://api.test/order", customTag = "CustomTag"
        )
    }

    @Test
    fun logResponseDetails_withHeadersAndBody_doesNotCrash() {
        ApiLoggingUtils.logResponseDetails(
            code = 200,
            message = "OK",
            headers = """{"Set-Cookie":"session=abc"}""",
            body = """{"otp":"123456"}"""
        )
    }

    @Test
    fun logResponseDetails_errorCodeWithoutBody_doesNotCrash() {
        ApiLoggingUtils.logResponseDetails(code = 500, message = "Internal Server Error")
    }

    @Test
    fun logSimpleApiCall_doesNotCrash() {
        ApiLoggingUtils.logSimpleApiCall("GET", "https://api.test/order", 200)
        ApiLoggingUtils.logSimpleApiCall("POST", "https://api.test/order", 404, customTag = "CustomTag")
    }
}

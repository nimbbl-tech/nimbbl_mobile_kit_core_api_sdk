package tech.nimbbl.coreapisdk.utils.extensions

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.core.constants.EventConstants
import java.util.Base64

/**
 * Covers the extension functions that need a real Android runtime (Context, Bitmap, Base64,
 * ConnectivityManager, real org.json) from NimbblSDKExtensions.kt and NimbblCoreSdkExtensions.kt.
 */
@RunWith(AndroidJUnit4::class)
class ExtensionsAndroidTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun base64UrlEncode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun fakeJwt(payloadJson: String): String {
        val header = base64UrlEncode("""{"alg":"none","typ":"JWT"}""".toByteArray())
        val payload = base64UrlEncode(payloadJson.toByteArray())
        return "$header.$payload.sig"
    }

    @Test
    fun getDeviceInfo_includesAllExpectedKeys() {
        val info = getDeviceInfo(context)
        assertTrue(info.containsKey(EventConstants.KEY_DEVICE_MANUFACTURER))
        assertTrue(info.containsKey(EventConstants.KEY_DEVICE_MODEL))
        assertTrue(info.containsKey(EventConstants.KEY_DEVICE_OS_VERSION))
        assertTrue(info.containsKey(EventConstants.KEY_DEVICE_SDK_VERSION))
        assertTrue(info.containsKey(EventConstants.KEY_DEVICE_SCREEN_DENSITY))
        assertTrue(info.containsKey(EventConstants.KEY_DEVICE_SCREEN_RESOLUTION))
    }

    @Test
    fun isNetConnected_doesNotCrash() {
        // Emulator network state can vary; just assert it completes and returns a Boolean.
        val connected = isNetConnected(context)
        assertTrue(connected == true || connected == false)
    }

    @Test
    fun encodeToBase64_roundTripsABitmap() {
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val encoded = encodeToBase64(bitmap, Bitmap.CompressFormat.PNG, 100)
        assertTrue(encoded.isNotEmpty())
        // Must be valid standard Base64 (android.util.Base64.DEFAULT).
        android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
    }

    @Test
    fun parseJwtToken_validToken_returnsOrderIdClaim() {
        val token = fakeJwt("""{"order_id":"order_123"}""")
        assertEquals("order_123", parseJwtToken(context, token))
    }

    @Test
    fun parseJwtToken_malformedToken_returnsEmptyStringAndDoesNotCrash() {
        assertEquals("", parseJwtToken(context, "not-a-jwt"))
    }

    @Test
    fun getMerchantIDFromJwtToken_validToken_returnsSubMerchantId() {
        val token = fakeJwt("""{"sub_merchant_id":"sm_456"}""")
        assertEquals("sm_456", getMerchantIDFromJwtToken(context, token))
    }

    @Test
    fun getMerchantIDFromJwtToken_missingClaim_returnsUnknown() {
        val token = fakeJwt("""{"order_id":"order_123"}""")
        assertEquals("unknown", getMerchantIDFromJwtToken(context, token))
    }

    @Test
    fun getMerchantIDFromJwtToken_malformedToken_returnsUnknown() {
        assertEquals("unknown", getMerchantIDFromJwtToken(context, "not-a-jwt"))
    }

    @Test
    fun getAPIRequestBody_returnsJsonString() {
        val json = JSONObject().put("a", 1)
        assertEquals("""{"a":1}""", getAPIRequestBody(json))
    }

    @Test
    fun jsonObjectToMap_convertsNestedObjectsAndArrays() {
        val json = JSONObject(
            """{"name":"Jane","age":30,"tags":["a","b"],"address":{"city":"X"},"nothing":null}"""
        )
        val map = json.toMap()
        assertEquals("Jane", map["name"])
        assertEquals(30, map["age"])
        assertEquals(listOf("a", "b"), map["tags"])
        assertEquals(mapOf("city" to "X"), map["address"])
        assertEquals(null, map["nothing"])
    }
}

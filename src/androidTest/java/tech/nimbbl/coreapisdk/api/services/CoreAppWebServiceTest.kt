package tech.nimbbl.coreapisdk.api.services

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.testutil.LocalHttpServer

/**
 * Exercises the real CoreAppWebService -> HttpConnectionHelper -> HttpURLConnection chain
 * against a real local socket (see LocalHttpServer), since HttpConnectionHelper is a Kotlin
 * `object` and can't be mocked on Android (mockStatic needs the inline mock maker, which
 * doesn't work on ART — see NimbblRepositoryImplTest).
 */
@RunWith(AndroidJUnit4::class)
class CoreAppWebServiceTest {

    private lateinit var server: LocalHttpServer
    private lateinit var service: CoreAppWebService

    @Before
    fun setUp() {
        server = LocalHttpServer()
        service = CoreAppWebService(
            baseUrl = server.baseUrl,
            userAgent = "test-agent",
            md5 = "fp_123",
            ipAddress = "127.0.0.1"
        )!!
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun getOrderDetails_mergesDefaultAndAuthHeaders() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"order_id":"o1"}"""))

        val response = service.getOrderDetails(server.baseUrl + "order", "Bearer tok_1")

        val recorded = server.takeRequest()!!
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertEquals("fp_123", recorded.headers["fingerPrint"])
        assertEquals("127.0.0.1", recorded.headers["ipAddress"])

        assertTrue(response.isSuccessful)
        assertEquals("o1", response.body?.getString("order_id"))
    }

    @Test
    fun resolveUser_sendsNimbblKeyAndAuthHeaders_andPostsBody() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"next_step":"otp_validation"}"""))

        val response = service.resolveUser(
            server.baseUrl + "resolve-user", "key_1", "Bearer tok_1", """{"mobile_number":"9876543210"}"""
        )

        val recorded = server.takeRequest()!!
        assertEquals("POST", recorded.method)
        assertEquals("key_1", recorded.headers["x-nimbbl-key"])
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertEquals("""{"mobile_number":"9876543210"}""", recorded.body)

        assertEquals("otp_validation", response.body?.getString("next_step"))
    }

    @Test
    fun getPaymentModes_sendsAllThreeHeaders() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))

        service.getPaymentModes(server.baseUrl, "key_1", "Bearer tok_1", "user_tok_1", "{}")

        val recorded = server.takeRequest()!!
        assertEquals("key_1", recorded.headers["x-nimbbl-key"])
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertEquals("user_tok_1", recorded.headers["x-nimbbl-user-token"])
    }

    @Test
    fun parseResponse_nonJsonSuccessBody_wrapsRawText() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "not-json"))

        val response = service.getPublicKey(server.baseUrl)

        assertTrue(response.isSuccessful)
        assertEquals("not-json", response.body?.getString("raw"))
    }

    @Test
    fun parseResponse_errorStatus_wrapsCodeAndBodyAsError() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(401, "Unauthorized", body = "invalid token"))

        val response = service.getPublicKey(server.baseUrl)

        assertFalse(response.isSuccessful)
        assertEquals(401, response.body?.getInt("status"))
        assertEquals(false, response.body?.getBoolean("success"))
        assertEquals("invalid token", response.body?.getString("error"))
    }

    @Test
    fun downloadBankLogo_returnsRawBytesOnSuccess() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "png-bytes"))

        val bytes = service.downloadBankLogo(server.baseUrl + "logo.png")

        assertEquals("png-bytes", bytes?.toString(Charsets.UTF_8))
    }

    @Test
    fun updateTransaction_usesPutMethod() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"status":"updated"}"""))

        service.updateTransaction(server.baseUrl, "Bearer tok_1", """{"status":"x"}""")

        assertEquals("PUT", server.takeRequest()!!.method)
    }

    @Test
    fun updateOrder_usesPatchMethod() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))

        service.updateOrder(server.baseUrl, "Bearer tok_1", """{"a":1}""")

        assertEquals("PATCH", server.takeRequest()!!.method)
    }

    @Test
    fun cancelCheckout_postsBodyWithAuthHeader() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))

        service.cancelCheckout(server.baseUrl, "Bearer tok_1", """{"command":"order_cancel"}""")

        val recorded = server.takeRequest()!!
        assertEquals("POST", recorded.method)
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertEquals("""{"command":"order_cancel"}""", recorded.body)
    }

    @Test
    fun checkOutResource_isGetWithNimbblKeyAndAuthHeaders() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"datetime":"2024-01-01"}"""))

        val response = service.checkOutResource(server.baseUrl, "key_1", "Bearer tok_1")

        val recorded = server.takeRequest()!!
        assertEquals("GET", recorded.method)
        assertEquals("key_1", recorded.headers["x-nimbbl-key"])
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertEquals("2024-01-01", response.body?.getString("datetime"))
    }

    @Test
    fun verifyUser_sendsNimbblKeyAndAuthHeaders() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"success":true}"""))

        service.verifyUser(server.baseUrl, "key_1", "Bearer tok_1", """{"otp":"123456"}""")

        val recorded = server.takeRequest()!!
        assertEquals("key_1", recorded.headers["x-nimbbl-key"])
        assertEquals("""{"otp":"123456"}""", recorded.body)
    }

    @Test
    fun getListOfBanks_postsOrderIdPayload() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"bank_list":[]}"""))

        val response = service.getListOfBanks(server.baseUrl, "key_1", "Bearer tok_1", """{"order_id":"order_1"}""")

        assertEquals("POST", server.takeRequest()!!.method)
        assertTrue(response.body!!.has("bank_list"))
    }

    @Test
    fun getListOfWallets_postsOrderIdPayload() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"wallet_list":[]}"""))

        val response = service.getListOfWallets(server.baseUrl, "key_1", "Bearer tok_1", """{"order_id":"order_1"}""")

        assertEquals("POST", server.takeRequest()!!.method)
        assertTrue(response.body!!.has("wallet_list"))
    }

    @Test
    fun initiatePayment_sendsAllThreeHeaders() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"status":"SUCCESS"}"""))

        service.initiatePayment(server.baseUrl, "key_1", "Bearer tok_1", "user_tok_1", """{"payment_mode":"UPI"}""")

        val recorded = server.takeRequest()!!
        assertEquals("key_1", recorded.headers["x-nimbbl-key"])
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertEquals("user_tok_1", recorded.headers["x-nimbbl-user-token"])
    }

    @Test
    fun makePayment_sendsAllThreeHeaders() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"status":"SUCCESS"}"""))

        service.makePayment(server.baseUrl, "key_1", "Bearer tok_1", "user_tok_1", """{"otp":"123456"}""")

        val recorded = server.takeRequest()!!
        assertEquals("user_tok_1", recorded.headers["x-nimbbl-user-token"])
    }

    @Test
    fun getTransactionEnquiry_onlySendsAuthHeader_noNimbblKey() = runTest {
        server.enqueueResponse(
            LocalHttpServer.CannedResponse(200, "OK", body = """{"order":{"status":"SUCCESS"}}""")
        )

        val response = service.getTransactionEnquiry(server.baseUrl, "Bearer tok_1", """{"order_id":"order_1"}""")

        val recorded = server.takeRequest()!!
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])
        assertTrue(recorded.headers.containsKey("x-nimbbl-key").not())
        assertTrue(response.body!!.has("order"))
    }

    @Test
    fun getBinData_postsCardOrderPayload() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"data":{"issuingBank":"HDFC"}}"""))

        service.getBinData(server.baseUrl, "key_1", "Bearer tok_1", """{"card_no":"4111111111111111"}""")

        assertEquals("""{"card_no":"4111111111111111"}""", server.takeRequest()!!.body)
    }

    @Test
    fun resendOtp_postsOrderAndTransactionPayload() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"otp_sent":true}"""))

        val response = service.resendOtp(server.baseUrl, "key_1", "Bearer tok_1", """{"order_id":"order_1"}""")

        assertEquals(true, response.body?.getBoolean("otp_sent"))
    }
}

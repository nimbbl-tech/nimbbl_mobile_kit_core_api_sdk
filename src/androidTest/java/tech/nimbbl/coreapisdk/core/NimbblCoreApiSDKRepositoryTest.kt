package tech.nimbbl.coreapisdk.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants
import tech.nimbbl.coreapisdk.testutil.LocalHttpServer

/**
 * Exercises NimbblCoreApiSDK's lazy repository-init path (getAPIRepositoryInstance ->
 * CoreAppWebService -> HttpConnectionHelper) against a real LocalHttpServer, by pointing the
 * global ServiceConstants.BASE_URL at it.
 *
 * `nimbblApiRepository` is a companion-level singleton with no reset API, and other test classes
 * (NimbblCoreApiSDKTest) also trigger its lazy init — on a shared instrumentation process that
 * would make this test see a stale repository pointed at whatever BASE_URL ran first. It's reset
 * via reflection in setUp()/tearDown() so this test is self-contained regardless of run order.
 */
@RunWith(AndroidJUnit4::class)
class NimbblCoreApiSDKRepositoryTest {

    private lateinit var server: LocalHttpServer
    private val originalBaseUrl = ServiceConstants.BASE_URL

    @Before
    fun setUp() {
        resetRepositorySingleton()
        server = LocalHttpServer()
        ServiceConstants.BASE_URL = server.baseUrl
        ServiceConstants.FINGERPRINT = "fp_test"
        ServiceConstants.DEVICE_FINGERPRINT = "dfp_test"
    }

    @After
    fun tearDown() {
        resetRepositorySingleton()
        server.close()
        ServiceConstants.BASE_URL = originalBaseUrl
    }

    private fun resetRepositorySingleton() {
        // The backing field's actual location (outer class vs Companion class) is a Kotlin
        // compiler implementation detail, so try both rather than hardcoding one.
        val outerField = runCatching {
            NimbblCoreApiSDK::class.java.getDeclaredField("nimbblApiRepository")
        }.getOrNull()
        if (outerField != null) {
            outerField.isAccessible = true
            outerField.set(null, null)
            return
        }
        val companion = NimbblCoreApiSDK::class.java.getDeclaredField("Companion").apply { isAccessible = true }.get(null)
        companion.javaClass.getDeclaredField("nimbblApiRepository").apply { isAccessible = true }.set(companion, null)
    }

    @Test
    fun getTransactionEnquiry_hitsRealServerAndMapsResponse() = runTest {
        server.enqueueResponse(
            LocalHttpServer.CannedResponse(200, "OK", body = """{"order":{"status":"SUCCESS"}}""")
        )

        val result = NimbblCoreApiSDK.getInstance().getTransactionEnquiry(
            token = "tok_1", orderId = "order_1", invoiceId = "inv_1", transactionId = "txn_1"
        )

        val recorded = server.takeRequest()!!
        assertTrue(recorded.path.endsWith("transaction-enquiry"))
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])

        assertTrue(result!!.isSuccessful)
        assertEquals("SUCCESS", result.data?.order?.status)
    }

    @Test
    fun updateOrder_hitsRealServerWithPatch() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"order_id":"order_1"}"""))

        val result = NimbblCoreApiSDK.getInstance().updateOrder(
            token = "tok_1", orderId = "order_1", callbackMode = "callback_mobile", platformType = "android"
        )

        val recorded = server.takeRequest()!!
        assertEquals("PATCH", recorded.method)
        assertTrue(recorded.path.endsWith("order"))

        assertTrue(result!!.isSuccessful)
        assertEquals("order_1", result.data?.order_id)
    }

    @Test
    fun getAPIRepositoryInstance_reusesSameRepositoryAcrossCalls() = runTest {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))
        val first = NimbblCoreApiSDK.getAPIRepositoryInstance()

        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))
        val second = NimbblCoreApiSDK.getAPIRepositoryInstance()

        org.junit.Assert.assertSame(first, second)
    }
}

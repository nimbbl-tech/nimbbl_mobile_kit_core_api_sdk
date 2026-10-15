package tech.nimbbl.coreapisdk.utils.logging

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.testutil.LocalHttpServer

/**
 * logEvent() fires a non-blocking coroutine, so there's no handle to await — LocalHttpServer's
 * takeRequest(timeout) naturally absorbs that by blocking until the background post arrives (or
 * the test fails on timeout), rather than needing an artificial sleep.
 */
@RunWith(AndroidJUnit4::class)
class EventLoggingServiceTest {

    private lateinit var server: LocalHttpServer

    @Before
    fun setUp() {
        server = LocalHttpServer()
        EventLoggingService.eventLogUrlOverride = server.baseUrl + "v1/log"
        EventLoggingService.setEventLoggingEnabled(true)
    }

    @After
    fun tearDown() {
        EventLoggingService.eventLogUrlOverride = null
        server.close()
    }

    @Test
    fun logEvent_postsEventDataToEventLogUrl() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        EventLoggingService.getInstance().logEvent(
            context = context,
            eventName = "checkout_initiated",
            orderId = "order_1",
            token = "tok_1"
        )

        val recorded = server.takeRequest(timeoutSeconds = 10)!!
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path.startsWith("/v1/log"))

        val sentJson = JSONObject(recorded.body)
        assertEquals("checkout_initiated", sentJson.getString("name"))
        assertEquals("order_1", sentJson.getString("orderId"))
    }

    @Test
    fun logEvent_masksTokenInSentPayload() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        EventLoggingService.getInstance().logEvent(
            context = context,
            eventName = "payment_status",
            token = "super_secret_token_value_123456"
        )

        val recorded = server.takeRequest(timeoutSeconds = 10)!!
        assertTrue("raw token must not appear in the sent payload", recorded.body.contains("super_secret_token_value_123456").not())
    }

    @Test
    fun logEvent_whenLoggingDisabled_sendsNothing() {
        EventLoggingService.setEventLoggingEnabled(false)
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        EventLoggingService.getInstance().logEvent(context = context, eventName = "should_not_send")

        assertEquals(null, server.takeRequest(timeoutSeconds = 2))

        EventLoggingService.setEventLoggingEnabled(true) // don't leak state into other tests
    }

    @Test
    fun setAppCode_and_isEventLoggingEnabled_areReadable() {
        EventLoggingService.setAppCode("app_1")
        assertTrue(EventLoggingService.isEventLoggingEnabled())
    }
}

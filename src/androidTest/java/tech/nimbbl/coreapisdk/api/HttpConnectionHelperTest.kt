package tech.nimbbl.coreapisdk.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.testutil.LocalHttpServer

@RunWith(AndroidJUnit4::class)
class HttpConnectionHelperTest {

    private lateinit var server: LocalHttpServer

    @Before
    fun setUp() {
        server = LocalHttpServer()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun get_sendsMethodAndHeaders_andParsesSuccessResponse() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = """{"status":"ok"}"""))

        val response = HttpConnectionHelper.get(server.baseUrl + "orders/1", mapOf("Authorization" to "Bearer tok_1"))

        val recorded = server.takeRequest()!!
        assertEquals("GET", recorded.method)
        assertEquals("/orders/1", recorded.path)
        assertEquals("Bearer tok_1", recorded.headers["Authorization"])

        assertEquals(200, response.code)
        assertTrue(response.isSuccessful)
        assertEquals("""{"status":"ok"}""", response.body)
    }

    @Test
    fun post_sendsBodyAndContentLength() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(201, "Created", body = """{"id":"1"}"""))

        val response = HttpConnectionHelper.post(
            server.baseUrl + "orders",
            mapOf("x-nimbbl-key" to "key_1"),
            """{"amount":100}"""
        )

        val recorded = server.takeRequest()!!
        assertEquals("POST", recorded.method)
        assertEquals("key_1", recorded.headers["x-nimbbl-key"])
        assertEquals("""{"amount":100}""", recorded.body)
        assertEquals(recorded.body.toByteArray(Charsets.UTF_8).size.toString(), recorded.headers["Content-Length"])

        assertEquals(201, response.code)
        assertEquals("""{"id":"1"}""", response.body)
    }

    @Test
    fun post_withNullBody_sendsEmptyJsonObject() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))

        HttpConnectionHelper.post(server.baseUrl, body = null)

        val recorded = server.takeRequest()!!
        assertEquals("{}", recorded.body)
    }

    @Test
    fun put_usesPutMethod() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))
        HttpConnectionHelper.put(server.baseUrl, body = """{"a":1}""")
        assertEquals("PUT", server.takeRequest()!!.method)
    }

    @Test
    fun patch_usesPatchMethod() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "{}"))
        HttpConnectionHelper.patch(server.baseUrl, body = """{"a":1}""")
        assertEquals("PATCH", server.takeRequest()!!.method)
    }

    @Test
    fun request_errorStatus_readsBodyFromErrorStream() {
        server.enqueueResponse(
            LocalHttpServer.CannedResponse(404, "Not Found", body = """{"error":"missing"}""")
        )

        val response = HttpConnectionHelper.get(server.baseUrl)

        assertEquals(404, response.code)
        assertTrue(response.isSuccessful.not())
        assertEquals("""{"error":"missing"}""", response.body)
    }

    @Test
    fun request_connectionFailure_returnsCodeMinusOne() {
        // Nothing is listening on this port once the server is closed.
        val deadUrl = server.baseUrl
        server.close()

        val response = HttpConnectionHelper.get(deadUrl)

        assertEquals(-1, response.code)
    }

    @Test
    fun downloadBytes_returnsBodyBytesOnSuccess() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(200, "OK", body = "binary-ish-content"))

        val bytes = HttpConnectionHelper.downloadBytes(server.baseUrl + "logo.png")

        assertEquals("binary-ish-content", bytes?.toString(Charsets.UTF_8))
    }

    @Test
    fun downloadBytes_returnsNullOnErrorStatus() {
        server.enqueueResponse(LocalHttpServer.CannedResponse(500, "Server Error", body = "oops"))

        val bytes = HttpConnectionHelper.downloadBytes(server.baseUrl)

        assertNull(bytes)
    }
}

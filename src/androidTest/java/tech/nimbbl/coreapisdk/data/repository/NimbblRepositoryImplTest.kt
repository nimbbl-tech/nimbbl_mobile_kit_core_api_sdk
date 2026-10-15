package tech.nimbbl.coreapisdk.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.nimbbl.coreapisdk.api.RawApiResponse
import tech.nimbbl.coreapisdk.api.services.CoreAppWebService

/**
 * Mocks CoreAppWebService (the HTTP layer boundary NimbblRepositoryImpl depends on) so these
 * tests exercise real request-building and response-mapping logic without any real network call.
 * Runs as an instrumented test because JsonParser needs real org.json (no Robolectric here).
 */
@RunWith(AndroidJUnit4::class)
class NimbblRepositoryImplTest {

    private lateinit var apiService: CoreAppWebService
    private lateinit var repository: NimbblRepositoryImpl

    @Before
    fun setUp() {
        apiService = mock()
        repository = NimbblRepositoryImpl(apiService)
    }

    private fun successResponse(bodyJson: String, code: Int = 200): RawApiResponse =
        RawApiResponse(code = code, message = "OK", body = JSONObject(bodyJson), rawBodyString = bodyJson)

    private fun errorResponse(code: Int, message: String): RawApiResponse =
        RawApiResponse(code = code, message = message, body = null, rawBodyString = null)

    @Test
    fun getOrderDetails_mapsSuccessfulResponseToOrderResponse() = runTest {
        whenever(apiService.getOrderDetails(any(), any()))
            .thenReturn(successResponse("""{"order_id":"order_1","status":"created"}"""))

        val result = repository.getOrderDetails("https://api.test/order", "token_1")

        assertTrue(result.isSuccessful)
        assertEquals("order_1", result.data?.order_id)
        assertEquals("created", result.data?.status)
    }

    @Test
    fun getOrderDetails_passesBearerAuthHeader() = runTest {
        whenever(apiService.getOrderDetails(any(), any())).thenReturn(successResponse("{}"))

        repository.getOrderDetails("https://api.test/order", "token_123")

        val authCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).getOrderDetails(org.mockito.kotlin.eq("https://api.test/order"), authCaptor.capture())
        assertEquals("Bearer token_123", authCaptor.firstValue)
    }

    @Test
    fun getOrderDetails_errorResponse_returnsNullDataWithCodeAndMessage() = runTest {
        whenever(apiService.getOrderDetails(any(), any())).thenReturn(errorResponse(404, "Not Found"))

        val result = repository.getOrderDetails("https://api.test/order", "token_1")

        assertTrue(result.isError)
        assertNull(result.data)
        assertEquals(404, result.code)
        assertEquals("Not Found", result.message)
    }

    @Test
    fun updateCheckOutCancelReason_successfulResponse_returnsUnitSuccess() = runTest {
        whenever(apiService.cancelCheckout(any(), any(), any())).thenReturn(successResponse("{}"))

        val result = repository.updateCheckOutCancelReason("token_1", "order_1", "user requested")

        assertTrue(result.isSuccessful)
    }

    @Test
    fun updateCheckOutCancelReason_sendsOrderIdAndReasonInBody() = runTest {
        whenever(apiService.cancelCheckout(any(), any(), any())).thenReturn(successResponse("{}"))

        repository.updateCheckOutCancelReason("token_1", "order_42", "duplicate order")

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).cancelCheckout(any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("order_42", sentBody.getString("order_id"))
        assertEquals("duplicate order", sentBody.getString("cancellation_reason"))
    }

    @Test
    fun resolveUser_mapsResponseAndSendsMobileNumber() = runTest {
        whenever(apiService.resolveUser(any(), any(), any(), any()))
            .thenReturn(successResponse("""{"next_step":"otp_validation","success":true}"""))

        val result = repository.resolveUser(
            "https://api.test/resolve-user", "token_1", "key_1", "9876543210", true, "order_1"
        )

        assertTrue(result.isSuccessful)
        assertEquals("otp_validation", result.data?.next_step)

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).resolveUser(any(), any(), any(), bodyCaptor.capture())
        assertEquals("9876543210", JSONObject(bodyCaptor.firstValue).getString("mobile_number"))
    }

    @Test
    fun getPaymentModes_mapsListResponse() = runTest {
        whenever(apiService.getPaymentModes(any(), any(), any(), any(), any()))
            .thenReturn(successResponse("""{"data":[{"payment_mode":"UPI"}]}"""))

        val result = repository.getPaymentModes(
            "https://api.test/payment-modes", "token_1", "key_1", "order_1", "user_token_1"
        )

        assertTrue(result.isSuccessful)
        assertEquals(1, result.data?.size)
        assertEquals("UPI", result.data?.get(0)?.payment_mode)
    }

    @Test
    fun getListOfBanks_defaultsToEmptyListOnEmptyResponse() = runTest {
        whenever(apiService.getListOfBanks(any(), any(), any(), any())).thenReturn(successResponse("{}"))

        val result = repository.getListOfBanks("https://api.test/banks", "token_1", "key_1", "order_1")

        assertTrue(result.isSuccessful)
        assertTrue(result.data?.bank_list.isNullOrEmpty())
    }

    @Test
    fun getBinData_stripsSpacesFromCardNumber() = runTest {
        whenever(apiService.getBinData(any(), any(), any(), any()))
            .thenReturn(successResponse("""{"data":{"issuingBank":"HDFC"}}"""))

        repository.getBinData("https://api.test/bin-data", "token_1", "key_1", "order_1", "4111 1111 1111 1111")

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).getBinData(any(), any(), any(), bodyCaptor.capture())
        assertEquals("4111111111111111", JSONObject(bodyCaptor.firstValue).getString("card_no"))
    }

    @Test
    fun getTransactionEnquiry_mapsNestedOrderAndTransactionList() = runTest {
        whenever(apiService.getTransactionEnquiry(any(), any(), any()))
            .thenReturn(successResponse("""{"order":{"status":"SUCCESS"},"transaction":[{"transaction_type":"UPI"}]}"""))

        val result = repository.getTransactionEnquiry("token_1", "order_1", "inv_1", "txn_1")

        assertTrue(result.isSuccessful)
        assertEquals("SUCCESS", result.data?.order?.status)
        assertEquals(1, result.data?.transaction?.size)
    }

    @Test
    fun getPublicKey_mapsResponse() = runTest {
        whenever(apiService.getPublicKey(any())).thenReturn(successResponse("""{"public_key":"pk_123"}"""))

        val result = repository.getPublicKey("https://api.test/public-key")

        assertEquals("pk_123", result.data?.public_key)
    }

    @Test
    fun resendOtp_mapsResponse() = runTest {
        whenever(apiService.resendOtp(any(), any(), any(), any()))
            .thenReturn(successResponse("""{"otp_sent":true,"success":true}"""))

        val result = repository.resendOtp(
            "https://api.test/resend-otp", "token_1", "key_1", "order_1", "UPI", "txn_1"
        )

        assertEquals(true, result.data?.otp_sent)
    }

    @Test
    fun updateTransactionDetail_sendsErrorDetailsInBody() = runTest {
        whenever(apiService.updateTransaction(any(), any(), any()))
            .thenReturn(successResponse("""{"transaction_id":"txn_1","status":"FAILED"}"""))

        repository.updateTransactionDetail(
            "https://api.test/update-txn", "token_1", "txn_1",
            errorCode = "ERR_1", consumerMessage = "Payment failed", merchantMessage = "Card declined"
        )

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).updateTransaction(any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("ERR_1", sentBody.getString("nimbbl_error_code"))
        assertEquals("Card declined", sentBody.getString("nimbbl_merchant_message"))
    }

    @Test
    fun setSubMerchantId_and_getSubMerchantId_areNoOpStubs() {
        repository.setSubMerchantId("sub_1")
        assertEquals("", repository.getSubMerchantId())
    }

    @Test
    fun getCheckOutResource_mapsResponse() = runTest {
        whenever(apiService.checkOutResource(any(), any(), any()))
            .thenReturn(successResponse("""{"datetime":"2024-01-01"}"""))

        val result = repository.getCheckOutResource("https://api.test/checkout-resources", "token_1", "key_1")

        assertTrue(result.isSuccessful)
        assertEquals("2024-01-01", result.data?.datetime)
    }

    @Test
    fun getListOfWallets_defaultsToEmptyListOnEmptyResponse() = runTest {
        whenever(apiService.getListOfWallets(any(), any(), any(), any())).thenReturn(successResponse("{}"))

        val result = repository.getListOfWallets("https://api.test/wallets", "token_1", "key_1", "order_1")

        assertTrue(result.isSuccessful)
        assertTrue(result.data?.wallet_list.isNullOrEmpty())
    }

    @Test
    fun updateOrderDetails_defaultsReferrerPlatformVersionWhenEmpty() = runTest {
        whenever(apiService.updateOrder(any(), any(), any()))
            .thenReturn(successResponse("""{"order_id":"order_1"}"""))

        repository.updateOrderDetails("token_1", "order_1", "callback_mobile", "android", "")

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).updateOrder(any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("2.0.0", sentBody.getString("referrer_platform_version"))
        assertEquals("callback_mobile", sentBody.getString("callback_mode"))
    }

    @Test
    fun verifyUser_sendsMobileNumberAndOtp() = runTest {
        whenever(apiService.verifyUser(any(), any(), any(), any()))
            .thenReturn(successResponse("""{"success":true}"""))

        repository.verifyUser("https://api.test/verify-user", "token_1", "key_1", "9876543210", "123456", "order_1")

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).verifyUser(any(), any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("9876543210", sentBody.getString("mobile_number"))
        assertEquals("123456", sentBody.getString("otp"))
    }

    @Test
    fun initiatePayment_omitsOptionalFieldsWhenNullOrEmpty() = runTest {
        whenever(apiService.initiatePayment(any(), any(), any(), any(), any()))
            .thenReturn(successResponse("""{"status":"SUCCESS"}"""))

        repository.initiatePayment(
            "https://api.test/initiate-payment", "token_1", "key_1", "order_1",
            callbackUrl = null, paymentMode = "UPI", subPaymentMode = null,
            cardDetailJsonObj = null, upiId = null
        )

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).initiatePayment(any(), any(), any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("UPI", sentBody.getString("payment_mode"))
        assertTrue(sentBody.has("bank").not())
        assertTrue(sentBody.has("callback_url").not())
        assertTrue(sentBody.has("upi_id").not())
    }

    @Test
    fun initiatePayment_includesOptionalFieldsWhenPresent() = runTest {
        whenever(apiService.initiatePayment(any(), any(), any(), any(), any()))
            .thenReturn(successResponse("""{"status":"SUCCESS"}"""))

        repository.initiatePayment(
            "https://api.test/initiate-payment", "token_1", "key_1", "order_1",
            callbackUrl = "https://merchant.test/cb", paymentMode = "netbanking", subPaymentMode = "hdfc",
            cardDetailJsonObj = null, upiId = null
        )

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).initiatePayment(any(), any(), any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("hdfc", sentBody.getString("bank"))
        assertEquals("https://merchant.test/cb", sentBody.getString("callback_url"))
    }

    @Test
    fun makePayment_sendsAllPaymentFields() = runTest {
        whenever(apiService.makePayment(any(), any(), any(), any(), any()))
            .thenReturn(successResponse("""{"status":"SUCCESS"}"""))

        repository.makePayment(
            "https://api.test/payment", "token_1", "key_1", "order_1",
            paymentMode = "UPI", paymentType = "otp", otp = "123456",
            upiId = "user@upi", flow = "collect", transactionId = "txn_1"
        )

        val bodyCaptor = argumentCaptor<String>()
        org.mockito.kotlin.verify(apiService).makePayment(any(), any(), any(), any(), bodyCaptor.capture())
        val sentBody = JSONObject(bodyCaptor.firstValue)
        assertEquals("otp", sentBody.getString("payment_type"))
        assertEquals("user@upi", sentBody.getString("upi_id"))
        assertEquals("collect", sentBody.getString("flow"))
        assertEquals("txn_1", sentBody.getString("transaction_id"))
    }
}

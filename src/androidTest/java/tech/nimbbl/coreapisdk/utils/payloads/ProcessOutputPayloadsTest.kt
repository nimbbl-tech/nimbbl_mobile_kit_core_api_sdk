package tech.nimbbl.coreapisdk.utils.payloads

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.data.models.common.ResolveUserResponse
import tech.nimbbl.coreapisdk.data.models.payment.BinData
import tech.nimbbl.coreapisdk.data.models.user.User

/** Key/value literals below mirror PayloadKeys.kt's actual constant values (some differ from their field names, e.g. "nimbblPayload", "key_user_name", "issuerName"). */
@RunWith(AndroidJUnit4::class)
class ProcessOutputPayloadsTest {

    @Test
    fun getLoadingShowPayload_hasDisplayLoaderEvent() {
        val payload = ProcessOutputPayloads.getLoadingShowPayload()
        assertEquals("display_loader", payload.getString("event"))
    }

    @Test
    fun getLoadingHidePayload_hasHideLoaderEvent() {
        val payload = ProcessOutputPayloads.getLoadingHidePayload()
        assertEquals("hide_loader", payload.getString("event"))
    }

    @Test
    fun getExceptionOccurredPayload_extractsNestedErrorMessage() {
        val errorJson = """{"error":{"message":"card declined"}}"""
        val payload = ProcessOutputPayloads.getExceptionOccurredPayload("initiate_payment", "ERR_1", errorJson)
        val nimbblPayload = payload.getJSONObject("nimbblPayload")
        assertEquals("card declined", nimbblPayload.getString("errorMessage"))
        assertEquals("ERR_1", nimbblPayload.getString("errorCode"))
        assertEquals("initiate_payment", nimbblPayload.getString("action"))
    }

    @Test
    fun getExceptionOccurredPayload_fallsBackToRawMessageWhenNotJson() {
        val payload = ProcessOutputPayloads.getExceptionOccurredPayload("initiate_payment", "ERR_2", "plain text error")
        val nimbblPayload = payload.getJSONObject("nimbblPayload")
        assertEquals("plain text error", nimbblPayload.getString("errorMessage"))
    }

    @Test
    fun getInitiateOrderOutputPayload_pointsNextToPaymentModes() {
        val nimbblPayload = ProcessOutputPayloads.getInitiateOrderOutputPayload().getJSONObject("nimbblPayload")
        assertEquals("paymentModes", nimbblPayload.getString("next"))
        assertEquals("success", nimbblPayload.getString("status"))
    }

    @Test
    fun getPaymentModePayload_parsesPaymentModesArray() {
        val nimbblPayload = ProcessOutputPayloads.getPaymentModePayload("""[{"code":"upi"}]""")
            .getJSONObject("nimbblPayload")
        assertEquals(1, nimbblPayload.getJSONArray("paymentModes").length())
    }

    @Test
    fun getPaymentModePayload_omitsArrayWhenInputEmpty() {
        val nimbblPayload = ProcessOutputPayloads.getPaymentModePayload(null).getJSONObject("nimbblPayload")
        assertFalse(nimbblPayload.has("paymentModes"))
    }

    @Test
    fun getResolveUserOutputPayload_combinesFirstAndLastName() {
        val response = ResolveUserResponse(
            item = User(
                country_code = "+91", email = null, first_name = "Jane", id = "u1",
                last_name = "Doe", mobile_number = "9876543210", token = null,
                token_expiration = null, user_id = "u1"
            ),
            next_step = "payment_mode",
            otp_sent = null,
            success = true
        )
        val nimbblPayload = ProcessOutputPayloads.getResolveUserOutputPayload(response).getJSONObject("nimbblPayload")
        assertEquals("Jane Doe", nimbblPayload.getString("key_user_name"))
        assertEquals("9876543210", nimbblPayload.getString("mobile_number"))
    }

    @Test
    fun getResolveUserOutputPayload_treatsLiteralNullLastNameAsEmpty() {
        // Regression guard: upstream sometimes sends the string "null" instead of omitting the field.
        val response = ResolveUserResponse(
            item = User(
                country_code = null, email = null, first_name = "Jane", id = "u1",
                last_name = "null", mobile_number = "9876543210", token = null,
                token_expiration = null, user_id = "u1"
            ),
            next_step = null,
            otp_sent = null,
            success = true
        )
        val nimbblPayload = ProcessOutputPayloads.getResolveUserOutputPayload(response).getJSONObject("nimbblPayload")
        assertEquals("Jane", nimbblPayload.getString("key_user_name"))
        assertEquals("payment_mode", nimbblPayload.getString("next_step"))
    }

    @Test
    fun getResolveUserOutputPayload_handlesNullBody() {
        val nimbblPayload = ProcessOutputPayloads.getResolveUserOutputPayload(null).getJSONObject("nimbblPayload")
        assertFalse(nimbblPayload.has("key_user_name"))
        assertEquals("payment_mode", nimbblPayload.getString("next_step"))
    }

    @Test
    fun getPaymentEnquiryPayload_includesAllFields() {
        val nimbblPayload = ProcessOutputPayloads.getPaymentEnquiryPayload(
            "upi", "ok", "success", "order_1", "txn_1", "sig_1"
        ).getJSONObject("nimbblPayload")
        assertEquals("upi", nimbblPayload.getString("payment_mode"))
        assertEquals("order_1", nimbblPayload.getString("order_id"))
        assertEquals("txn_1", nimbblPayload.getString("transaction_id"))
        assertEquals("sig_1", nimbblPayload.getString("signature"))
    }

    @Test
    fun getInitiatePaymentResponsePayload_pointsNextToCompletePayment() {
        val nimbblPayload = ProcessOutputPayloads.getInitiatePaymentResponsePayload("card", "initiated", "txn_2")
            .getJSONObject("nimbblPayload")
        assertEquals("completePayment", nimbblPayload.getString("next"))
        assertEquals("txn_2", nimbblPayload.getString("transaction_id"))
    }

    @Test
    fun getBinDataResponsePayload_creditCardMapsSubPaymentCode() {
        val binData = BinData(issuingBank = "HDFC", cardCategory = "CC", n_card_type = "VISA")
        val nimbblPayload = ProcessOutputPayloads.getBinDataResponsePayload(binData).getJSONObject("nimbblPayload")
        assertEquals("HDFC", nimbblPayload.getString("issuerName"))
        assertEquals("VISA", nimbblPayload.getString("schemeName"))
        assertEquals("credit", nimbblPayload.getString("sub_payment_code"))
        assertEquals("Credit Card", nimbblPayload.getString("sub_payment_name"))
    }

    @Test
    fun getBinDataResponsePayload_handlesNullBody() {
        val nimbblPayload = ProcessOutputPayloads.getBinDataResponsePayload(null).getJSONObject("nimbblPayload")
        assertFalse(nimbblPayload.has("issuerName"))
    }

    @Test
    fun getResendOtpPayload_reflectsOtpSentFlag() {
        val nimbblPayload = ProcessOutputPayloads.getResendOtpPayload(true).getJSONObject("nimbblPayload")
        assertTrue(nimbblPayload.getBoolean("otp_sent"))
    }

    @Test
    fun getCardValidateDataResponsePayload_pointsNextToInitiatePayment() {
        val nimbblPayload = ProcessOutputPayloads.getCardValidateDataResponsePayload().getJSONObject("nimbblPayload")
        assertEquals("initiatePayment", nimbblPayload.getString("next"))
    }

    @Test
    fun getUpiIdResponsePayload_defaultsStatusWhenNull() {
        val nimbblPayload = ProcessOutputPayloads.getUpiIdResponsePayload(
            "upi", 1, "user@upi", null, "Jane Doe"
        ).getJSONObject("nimbblPayload")
        assertEquals("success", nimbblPayload.getString("status"))
        assertEquals("user@upi", nimbblPayload.getString("vpa_id"))
        assertEquals("Jane Doe", nimbblPayload.getString("vpa_account_holder"))
    }
}

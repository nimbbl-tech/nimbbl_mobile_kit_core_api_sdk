package tech.nimbbl.coreapisdk.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.api.models.responses.OrderResponse
import tech.nimbbl.coreapisdk.api.models.responses.UpdateTransactionResponse
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.data.models.common.CheckoutResourceVo
import tech.nimbbl.coreapisdk.data.models.common.InitiatePaymentResponse
import tech.nimbbl.coreapisdk.data.models.common.PublicKeyResponse
import tech.nimbbl.coreapisdk.data.models.common.ResendOtpResponse
import tech.nimbbl.coreapisdk.data.models.common.ResolveUserResponse
import tech.nimbbl.coreapisdk.data.models.payment.BinDataResponse
import tech.nimbbl.coreapisdk.data.models.payment.ListOfBankResponse
import tech.nimbbl.coreapisdk.data.models.payment.ListOfWalletResponse
import tech.nimbbl.coreapisdk.data.models.payment.PaymentModesResponse

/**
 * Uses real org.json (unavailable in plain JVM unit tests on this project without
 * Robolectric/a json test dependency), so this runs as an instrumented test.
 */
@RunWith(AndroidJUnit4::class)
class JsonParserTest {

    @Test
    fun parseOrderResponse_mapsUserAndAddressFieldsByJsonKey() {
        val json = JSONObject(
            """
            {
              "order_id": "order_123",
              "status": "created",
              "user": {
                "country_code": "+91",
                "email": "jane@example.com",
                "first_name": "Jane",
                "id": "user_1",
                "last_name": "Doe",
                "mobile_number": "9876543210",
                "token": "tok_abc",
                "token_expiration": "2026-12-31",
                "user_id": "u_1"
              },
              "address": {
                "address_1": "1080 Beach Mansion",
                "address_id": "addr_1",
                "address_type": "Beach House",
                "area": "Elyria",
                "city": "Atlantis",
                "landmark": "Opposite Magic Mountain",
                "pincode": "100389",
                "state": "Castalia",
                "street": "Magic Beach Drive"
              }
            }
            """.trimIndent()
        )

        val response = JsonParser.fromJson(json, OrderResponse::class.java) as OrderResponse

        assertEquals("order_123", response.order_id)
        assertEquals("created", response.status)

        val user = response.user!!
        assertEquals("+91", user.country_code)
        assertEquals("jane@example.com", user.email)
        assertEquals("Jane", user.first_name)
        assertEquals("Doe", user.last_name)
        assertEquals("9876543210", user.mobile_number)

        val address = response.address!!
        assertEquals("1080 Beach Mansion", address.address_1)
        assertEquals("Elyria", address.area)
        assertEquals("Atlantis", address.city)
        assertEquals("100389", address.pincode)
        assertEquals("Castalia", address.state)
        assertEquals("Magic Beach Drive", address.street)
    }

    @Test
    fun parseOrderResponse_handlesMissingUserAndAddress() {
        val json = JSONObject("""{"order_id": "order_456", "status": "pending"}""")

        val response = JsonParser.fromJson(json, OrderResponse::class.java) as OrderResponse

        assertEquals("order_456", response.order_id)
        assertNull(response.user)
        assertNull(response.address)
    }

    @Test
    fun fromJson_returnsNullForNullInput() {
        assertNull(JsonParser.fromJson(null, OrderResponse::class.java))
    }

    @Test
    fun parseCheckoutResourceVo_mapsNestedDataAndItems() {
        val json = JSONObject(
            """{"data":[{"display_tray":"t","items":[{"display_priority":1,"payment_mode":"UPI","extraInfo":{"next":"n"},"itemCol":[]}]}],"datetime":"2024-01-01"}"""
        )
        val response = JsonParser.fromJson(json, CheckoutResourceVo::class.java) as CheckoutResourceVo
        assertEquals("2024-01-01", response.datetime)
        val item = response.data!![0].items!![0]
        assertEquals(1, item.display_priority)
        assertEquals("UPI", item.payment_mode)
        assertEquals("n", item.extraInfo!!.next)
    }

    @Test
    fun parseResolveUserResponse_mapsUserItem() {
        val json = JSONObject(
            """{"item":{"first_name":"Jane","last_name":"Doe","mobile_number":"9876543210"},"next_step":"payment_mode","otp_sent":false,"success":true}"""
        )
        val response = JsonParser.fromJson(json, ResolveUserResponse::class.java) as ResolveUserResponse
        assertEquals("Jane", response.item!!.first_name)
        assertEquals("payment_mode", response.next_step)
        assertEquals(true, response.success)
    }

    @Test
    fun parseInitiatePaymentResponse_mapsNestedExtraInfoAndRequestArgs() {
        val json = JSONObject(
            """{"completion_time":1.5,"extra_info":{"attempts":1,"data":{"redirectUrl":"u"},"payment_partner":"p"},"info":{"request_args":{"order_id":"o","payment_mode":"UPI","transaction_id":"t"},"url":"u"},"message":"ok","order_id":"o","redirect_url":"u","status":"SUCCESS","status_code":200,"transaction_id":"t","vpa":"a@b","isVPAValid":1,"payerAccountName":"name"}"""
        )
        val response = JsonParser.fromJson(json, InitiatePaymentResponse::class.java) as InitiatePaymentResponse
        assertEquals("SUCCESS", response.status)
        assertEquals(200, response.status_code)
        assertEquals(1, response.extra_info!!.attempts)
        assertEquals("u", response.extra_info!!.data!!.redirectUrl)
        assertEquals("UPI", response.info!!.request_args!!.payment_mode)
        assertEquals("a@b", response.vpa)
    }

    @Test
    fun parsePublicKeyResponse_mapsKey() {
        val json = JSONObject("""{"public_key":"pk_test_xxx"}""")
        val response = JsonParser.fromJson(json, PublicKeyResponse::class.java) as PublicKeyResponse
        assertEquals("pk_test_xxx", response.public_key)
    }

    @Test
    fun parseBinDataResponse_mapsCardDetails() {
        val json = JSONObject(
            """{"data":{"issuingBank":"HDFC","cardCategory":"CREDIT","n_card_type":"VISA"},"status_code":200}"""
        )
        val response = JsonParser.fromJson(json, BinDataResponse::class.java) as BinDataResponse
        assertEquals("HDFC", response.data!!.issuingBank)
        assertEquals("CREDIT", response.data!!.cardCategory)
        assertEquals(200, response.status_code)
    }

    @Test
    fun parseUpdateTransactionResponse_mapsFlatFields() {
        val json = JSONObject(
            """{"transaction_id":"txn1","status":"SUCCESS","payment_mode":"UPI","grand_total_amount":100.0}"""
        )
        val response = JsonParser.fromJson(json, UpdateTransactionResponse::class.java) as UpdateTransactionResponse
        assertEquals("txn1", response.transaction_id)
        assertEquals("SUCCESS", response.status)
        assertEquals("UPI", response.payment_mode)
        assertEquals(100.0, response.grand_total_amount)
    }

    @Test
    fun parseTransactionEnquiryResponseVo_mapsOrderAndTransactionList() {
        val json = JSONObject(
            """{"order":{"invoice_id":"inv1","nimbbl_order_id":"NO1","status":"SUCCESS"},"transaction":[{"completion_time":5,"transaction_type":"UPI"}]}"""
        )
        val response = JsonParser.fromJson(json, TransactionEnquiryResponseVo::class.java) as TransactionEnquiryResponseVo
        assertEquals("NO1", response.order!!.nimbbl_order_id)
        assertEquals("SUCCESS", response.order!!.status)
        assertEquals(1, response.transaction!!.size)
        assertEquals("UPI", response.transaction!![0].transaction_type)
    }

    @Test
    fun parseResendOtpResponse_mapsFields() {
        val json = JSONObject("""{"otp_sent":true,"status_code":200,"success":true}""")
        val response = JsonParser.fromJson(json, ResendOtpResponse::class.java) as ResendOtpResponse
        assertEquals(true, response.otp_sent)
        assertEquals(200, response.status_code)
    }

    @Test
    fun parseListOfBankResponse_mapsBankList() {
        val json = JSONObject("""{"bank_list":[{"additinal_cahrges":"0","bank_name":"HDFC Bank"}]}""")
        val response = JsonParser.fromJson(json, ListOfBankResponse::class.java) as ListOfBankResponse
        assertEquals(1, response.bank_list.size)
        assertEquals("HDFC Bank", response.bank_list[0].bank_name)
    }

    @Test
    fun parseListOfBankResponse_defaultsToEmptyListWhenMissing() {
        val response = JsonParser.fromJson(JSONObject("{}"), ListOfBankResponse::class.java) as ListOfBankResponse
        assertTrue(response.bank_list.isEmpty())
    }

    @Test
    fun parseListOfWalletResponse_mapsWalletList() {
        val json = JSONObject("""{"wallet_list":[{"additinal_cahrges":"0","wallet_name":"Paytm"}]}""")
        val response = JsonParser.fromJson(json, ListOfWalletResponse::class.java) as ListOfWalletResponse
        assertEquals(1, response.wallet_list.size)
        assertEquals("Paytm", response.wallet_list[0].wallet_name)
    }

    @Test
    fun parsePaymentModesResponse_readsFromDataKey() {
        val json = JSONObject(
            """{"data":[{"payment_mode":"UPI","extra_info":{"next":"n","eta_completion":5,"server_intent":true}}]}"""
        )
        val response = JsonParser.fromJson(json, PaymentModesResponse::class.java) as PaymentModesResponse
        assertEquals(1, response.size)
        assertEquals("UPI", response[0].payment_mode)
        assertEquals(5, response[0].extra_info!!.eta_completion)
        assertEquals(true, response[0].extra_info!!.server_intent)
    }

    @Test
    fun parsePaymentModesResponse_fallsBackToPaymentModesKey() {
        val json = JSONObject("""{"payment_modes":[{"payment_mode":"wallet"}]}""")
        val response = JsonParser.fromJson(json, PaymentModesResponse::class.java) as PaymentModesResponse
        assertEquals(1, response.size)
        assertEquals("wallet", response[0].payment_mode)
    }
}

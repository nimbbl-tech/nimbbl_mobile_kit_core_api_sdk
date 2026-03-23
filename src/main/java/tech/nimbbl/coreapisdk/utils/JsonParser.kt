package tech.nimbbl.coreapisdk.utils

import org.json.JSONArray
import org.json.JSONObject
import tech.nimbbl.coreapisdk.api.models.responses.OrderResponse
import tech.nimbbl.coreapisdk.api.models.responses.UpdateTransactionResponse
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.Order
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.Transaction
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.data.models.common.CheckoutResourceVo
import tech.nimbbl.coreapisdk.data.models.common.Data
import tech.nimbbl.coreapisdk.data.models.common.Error
import tech.nimbbl.coreapisdk.data.models.common.ExtraInfo
import tech.nimbbl.coreapisdk.data.models.common.Info
import tech.nimbbl.coreapisdk.data.models.common.InitiatePaymentData
import tech.nimbbl.coreapisdk.data.models.common.InitiatePaymentExtraInfo
import tech.nimbbl.coreapisdk.data.models.common.InitiatePaymentResponse
import tech.nimbbl.coreapisdk.data.models.common.PublicKeyResponse
import tech.nimbbl.coreapisdk.data.models.common.RequestArgs
import tech.nimbbl.coreapisdk.data.models.common.ResendOtpResponse
import tech.nimbbl.coreapisdk.data.models.common.ResolveUserResponse
import tech.nimbbl.coreapisdk.data.models.order.Item
import tech.nimbbl.coreapisdk.data.models.order.OrderLineItem
import tech.nimbbl.coreapisdk.data.models.order.SubMerchant
import tech.nimbbl.coreapisdk.data.models.payment.Bank
import tech.nimbbl.coreapisdk.data.models.payment.BinData
import tech.nimbbl.coreapisdk.data.models.payment.BinDataResponse
import tech.nimbbl.coreapisdk.data.models.payment.ListOfBankResponse
import tech.nimbbl.coreapisdk.data.models.payment.ListOfWalletResponse
import tech.nimbbl.coreapisdk.data.models.payment.PaymentModesResponse
import tech.nimbbl.coreapisdk.data.models.payment.PaymentModesResponseItem
import tech.nimbbl.coreapisdk.data.models.payment.Wallet
import tech.nimbbl.coreapisdk.data.models.user.Address
import tech.nimbbl.coreapisdk.data.models.user.User

/**
 * JSON parsing using org.json only (no Gson).
 */
object JsonParser {

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key)) optString(key).takeIf { it.isNotEmpty() } ?: optString(key) else null
    private fun JSONObject.optIntOrNull(key: String): Int? = if (has(key)) optInt(key) else null
    private fun JSONObject.optLongOrNull(key: String): Long? = if (has(key)) optLong(key) else null
    private fun JSONObject.optDoubleOrNull(key: String): Double? = if (has(key)) optDouble(key) else null
    private fun JSONObject.optFloatOrNull(key: String): Float? = if (has(key)) optDouble(key).toFloat() else null
    private fun JSONObject.optBooleanOrNull(key: String): Boolean? = if (has(key)) optBoolean(key) else null
    private fun JSONObject.optJSONObjectOrNull(key: String): JSONObject? = if (has(key)) optJSONObject(key) else null
    private fun JSONObject.optJSONArrayOrNull(key: String): JSONArray? = if (has(key)) optJSONArray(key) else null

    fun fromJson(json: JSONObject?, type: Class<*>): Any? {
        if (json == null) return null
        return when (type) {
            CheckoutResourceVo::class.java -> parseCheckoutResourceVo(json)
            OrderResponse::class.java -> parseOrderResponse(json)
            ResolveUserResponse::class.java -> parseResolveUserResponse(json)
            InitiatePaymentResponse::class.java -> parseInitiatePaymentResponse(json)
            PublicKeyResponse::class.java -> parsePublicKeyResponse(json)
            BinDataResponse::class.java -> parseBinDataResponse(json)
            UpdateTransactionResponse::class.java -> parseUpdateTransactionResponse(json)
            TransactionEnquiryResponseVo::class.java -> parseTransactionEnquiryResponseVo(json)
            ResendOtpResponse::class.java -> parseResendOtpResponse(json)
            ListOfBankResponse::class.java -> parseListOfBankResponse(json)
            ListOfWalletResponse::class.java -> parseListOfWalletResponse(json)
            PaymentModesResponse::class.java -> parsePaymentModesResponse(json)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> fromJson(json: JSONObject?): T? {
        if (json == null) return null
        return fromJson(json, T::class.java) as? T
    }

    fun toJson(obj: Any?): String = when (obj) {
        is JSONObject -> obj.toString()
        else -> JSONObject().apply { put("value", obj.toString()) }.toString()
    }

    private fun parseUser(j: JSONObject?): User? {
        if (j == null) return null
        return User(
            country_code = j.optStringOrNull("country_code"),
            email = j.optStringOrNull("email"),
            first_name = j.optStringOrNull("first_name"),
            id = j.optStringOrNull("id"),
            last_name = j.optStringOrNull("last_name"),
            mobile_number = j.optStringOrNull("mobile_number"),
            token = j.optStringOrNull("token"),
            token_expiration = j.optStringOrNull("token_expiration"),
            user_id = j.optStringOrNull("user_id")
        )
    }

    private fun parseAddress(j: JSONObject?): Address? {
        if (j == null) return null
        return Address(
            address_1 = j.optStringOrNull("address_1"),
            address_id = j.optStringOrNull("address_id"),
            address_type = j.optStringOrNull("address_type"),
            area = j.optStringOrNull("area"),
            city = j.optStringOrNull("city"),
            landmark = j.optStringOrNull("landmark"),
            pincode = j.optStringOrNull("pincode"),
            state = j.optStringOrNull("state"),
            street = j.optStringOrNull("street")
        )
    }

    private fun parseOrderLineItem(j: JSONObject?): OrderLineItem? {
        if (j == null) return null
        return OrderLineItem(
            amount_before_tax = j.opt("amount_before_tax"),
            description = j.optStringOrNull("description"),
            image_url = j.optStringOrNull("image_url"),
            order_id = j.optIntOrNull("order_id"),
            quantity = j.optIntOrNull("quantity"),
            rate = j.optDoubleOrNull("rate"),
            sku_id = j.opt("sku_id"),
            tax = j.opt("tax"),
            title = j.optStringOrNull("title"),
            total_amount = j.optDoubleOrNull("total_amount"),
            uom = j.opt("uom")
        )
    }

    private fun parseSubMerchant(j: JSONObject?): SubMerchant? {
        if (j == null) return null
        return SubMerchant(
            description = j.optStringOrNull("description"),
            sandbox = j.optStringOrNull("sandbox"),
            sub_merchant_id = j.optStringOrNull("sub_merchant_id")
        )
    }

    private fun parseOrderResponse(j: JSONObject): OrderResponse {
        val orderLineItemArr = j.optJSONArrayOrNull("order_line_item")
        val orderLineItems = orderLineItemArr?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                (arr.opt(i) as? JSONObject)?.let { parseOrderLineItem(it) }
            }
        }
        return OrderResponse(
            additional_charges = j.optDoubleOrNull("additional_charges"),
            address = parseAddress(j.optJSONObjectOrNull("address")),
            amount_before_tax = j.optDoubleOrNull("amount_before_tax"),
            attempts = j.optIntOrNull("attempts"),
            browser_name = j.optStringOrNull("browser_name"),
            callback_mode = j.optStringOrNull("callback_mode"),
            callback_url = j.optStringOrNull("callback_url"),
            cancellation_reason = j.opt("cancellation_reason"),
            currency = j.optStringOrNull("currency"),
            description = j.opt("description"),
            device_name = j.optStringOrNull("device_name"),
            device_user_agent = j.optStringOrNull("device_user_agent"),
            fingerprint = j.opt("fingerprint"),
            grand_total_amount = j.optDoubleOrNull("grand_total_amount"),
            invoice_id = j.optStringOrNull("invoice_id"),
            max_retries = j.optIntOrNull("max_retries"),
            merchant_shopfront_domain = j.optStringOrNull("merchant_shopfront_domain"),
            order_date = j.optStringOrNull("order_date"),
            order_from_ip = j.optStringOrNull("order_from_ip"),
            order_id = j.optStringOrNull("order_id"),
            order_line_item = orderLineItems,
            order_transac_type = j.opt("order_transac_type"),
            os_name = j.optStringOrNull("os_name"),
            partner_id = j.opt("partner_id"),
            referrer_platform = j.opt("referrer_platform")?.toString(),
            referrer_platform_version = j.opt("referrer_platform_version")?.toString(),
            status = j.optStringOrNull("status"),
            sub_merchant = parseSubMerchant(j.optJSONObjectOrNull("sub_merchant")),
            sub_merchant_id = j.optIntOrNull("sub_merchant_id"),
            tax = j.optDoubleOrNull("tax"),
            total_amount = j.optDoubleOrNull("total_amount"),
            user = parseUser(j.optJSONObjectOrNull("user"))
        )
    }

    private fun parseExtraInfo(j: JSONObject?): ExtraInfo? {
        if (j == null) return null
        return ExtraInfo(
            additional_charges = j.optStringOrNull("additional_charges"),
            auto_debit_flow_possible = j.optStringOrNull("auto_debit_flow_possible"),
            redirection_url = j.optStringOrNull("redirection_url"),
            eta_completion = j.optIntOrNull("eta_completion"),
            next = j.optStringOrNull("next"),
            vpa_account_holder = j.optStringOrNull("vpa_account_holder"),
            vpa_id = j.optStringOrNull("vpa_id"),
            vpa_provider = j.optStringOrNull("vpa_provider"),
            app_package_name = j.optStringOrNull("app_package_name"),
            payment_type = j.optStringOrNull("payment_type"),
            server_intent = j.optBooleanOrNull("server_intent")
        )
    }

    private fun parseItem(j: JSONObject?): Item? {
        if (j == null) return null
        val itemColArr = j.optJSONArrayOrNull("itemCol")
        val itemCol = itemColArr?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                (arr.opt(i) as? JSONObject)?.let { parseItem(it) }
            }
        }
        return Item(
            display_priority = j.optIntOrNull("display_priority"),
            display_tray = j.optStringOrNull("display_tray"),
            flow = j.optStringOrNull("flow"),
            items = j.opt("items"),
            logo_url = j.optStringOrNull("logo_url"),
            payment_mode = j.optStringOrNull("payment_mode"),
            payment_mode_code = j.optStringOrNull("payment_mode_code"),
            sub_payment_code = j.optStringOrNull("sub_payment_code"),
            sub_payment_name = j.optStringOrNull("sub_payment_name"),
            app_code = j.optStringOrNull("app_code"),
            app_name = j.optStringOrNull("app_name"),
            app_package_name = j.optStringOrNull("app_package_name"),
            extraInfo = parseExtraInfo(j.optJSONObjectOrNull("extraInfo") ?: j.optJSONObjectOrNull("extra_info")),
            additional_charges = j.optStringOrNull("additional_charges"),
            next = j.optStringOrNull("next"),
            itemCol = itemCol
        )
    }

    private fun parseData(j: JSONObject?): Data? {
        if (j == null) return null
        val itemsArr = j.optJSONArrayOrNull("items")
        val items = itemsArr?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                (arr.opt(i) as? JSONObject)?.let { parseItem(it) }
            }
        }
        return Data(
            display_tray = j.optStringOrNull("display_tray"),
            items = items
        )
    }

    private fun parseCheckoutResourceVo(j: JSONObject): CheckoutResourceVo? {
        val dataArr = j.optJSONArrayOrNull("data")
        val dataList = dataArr?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                (arr.opt(i) as? JSONObject)?.let { parseData(it) }
            }
        }
        return CheckoutResourceVo(
            `data` = dataList,
            datetime = j.optStringOrNull("datetime")
        )
    }

    private fun parseResolveUserResponse(j: JSONObject): ResolveUserResponse =
        ResolveUserResponse(
            item = parseUser(j.optJSONObjectOrNull("item")),
            next_step = j.optStringOrNull("next_step"),
            otp_sent = j.optBooleanOrNull("otp_sent"),
            success = j.optBooleanOrNull("success")
        )

    private fun parseRequestArgs(j: JSONObject?): RequestArgs? {
        if (j == null) return null
        return RequestArgs(
            order_id = j.optStringOrNull("order_id"),
            payment_mode = j.optStringOrNull("payment_mode"),
            transaction_id = j.optStringOrNull("transaction_id")
        )
    }

    private fun parseInfo(j: JSONObject?): Info? {
        if (j == null) return null
        return Info(
            request_args = parseRequestArgs(j.optJSONObjectOrNull("request_args")),
            url = j.optStringOrNull("url")
        )
    }

    private fun parseInitiatePaymentData(j: JSONObject?): InitiatePaymentData? {
        if (j == null) return null
        return InitiatePaymentData(
            redirectUrl = j.optStringOrNull("redirectUrl") ?: j.optStringOrNull("redirect_url")
        )
    }

    private fun parseInitiatePaymentExtraInfo(j: JSONObject?): InitiatePaymentExtraInfo? {
        if (j == null) return null
        return InitiatePaymentExtraInfo(
            attempts = j.optIntOrNull("attempts"),
            `data` = parseInitiatePaymentData(j.optJSONObjectOrNull("data")),
            payment_partner = j.optStringOrNull("payment_partner")
        )
    }

    private fun parseInitiatePaymentResponse(j: JSONObject): InitiatePaymentResponse =
        InitiatePaymentResponse(
            completion_time = j.optFloatOrNull("completion_time"),
            extra_info = parseInitiatePaymentExtraInfo(j.optJSONObjectOrNull("extra_info")),
            info = parseInfo(j.optJSONObjectOrNull("info")),
            message = j.optStringOrNull("message"),
            order_id = j.optStringOrNull("order_id"),
            redirect_url = j.optStringOrNull("redirect_url"),
            status = j.optStringOrNull("status"),
            status_code = j.optIntOrNull("status_code"),
            transaction_id = j.optStringOrNull("transaction_id"),
            vpa = j.optStringOrNull("vpa"),
            isVPAValid = j.optIntOrNull("isVPAValid"),
            payerAccountName = j.optStringOrNull("payerAccountName")
        )

    private fun parsePublicKeyResponse(j: JSONObject): PublicKeyResponse =
        PublicKeyResponse(public_key = j.optStringOrNull("public_key"))

    private fun parseError(j: JSONObject?): Error? {
        if (j == null) return null
        return Error(
            attempts = j.optIntOrNull("attempts"),
            c_message = j.optStringOrNull("c_message"),
            code = j.optStringOrNull("code"),
            m_message = j.optStringOrNull("m_message"),
            raise_alarm = j.optBooleanOrNull("raise_alarm"),
            retry_allowed = j.optBooleanOrNull("retry_allowed"),
            status = j.optStringOrNull("status"),
            status_code = j.optIntOrNull("status_code")
        )
    }

    private fun parseBinData(j: JSONObject?): BinData? {
        if (j == null) return null
        return BinData(
            issuingBank = j.optStringOrNull("issuingBank"),
            cardCategory = j.optStringOrNull("cardCategory"),
            n_card_type = j.optStringOrNull("n_card_type")
        )
    }

    private fun parseBinDataResponse(j: JSONObject): BinDataResponse =
        BinDataResponse(
            `data` = parseBinData(j.optJSONObjectOrNull("data")),
            error = parseError(j.optJSONObjectOrNull("error")),
            status_code = j.optIntOrNull("status_code")
        )

    private fun parseUpdateTransactionResponse(j: JSONObject): UpdateTransactionResponse {
        return UpdateTransactionResponse(
            additional_charges = j.optDoubleOrNull("additional_charges"),
            bank_name = j.optStringOrNull("bank_name"),
            expiry = j.optStringOrNull("expiry"),
            freecharge_payment_mode = j.optStringOrNull("freecharge_payment_mode"),
            grand_total_amount = j.optDoubleOrNull("grand_total_amount"),
            holder_name = j.optStringOrNull("holder_name"),
            id = j.optIntOrNull("id"),
            issuer = j.optStringOrNull("issuer"),
            masked_card = j.optStringOrNull("masked_card"),
            merchant_callback_url = j.optStringOrNull("merchant_callback_url"),
            merchant_id = j.optIntOrNull("merchant_id"),
            network = j.optStringOrNull("network"),
            nimbbl_consumer_message = j.optStringOrNull("nimbbl_consumer_message"),
            nimbbl_error_code = j.optStringOrNull("nimbbl_error_code"),
            nimbbl_merchant_message = j.optStringOrNull("nimbbl_merchant_message"),
            order_id = j.optIntOrNull("order_id"),
            orignal_payment_transaction_id = j.optStringOrNull("orignal_payment_transaction_id"),
            payment_mode = j.optStringOrNull("payment_mode"),
            payment_partner = j.optStringOrNull("payment_partner"),
            paytm_payment_mode = j.optStringOrNull("paytm_payment_mode"),
            psp_generated_redirect = j.optStringOrNull("psp_generated_redirect"),
            psp_generated_redirect_type = j.optStringOrNull("psp_generated_redirect_type"),
            psp_generated_txn_id = j.optStringOrNull("psp_generated_txn_id"),
            refund_amount = j.optStringOrNull("refund_amount"),
            refund_arn = j.optStringOrNull("refund_arn"),
            refund_done = j.optStringOrNull("refund_done"),
            retry_allowed = j.optStringOrNull("retry_allowed"),
            status = j.optStringOrNull("status"),
            sub_merchant_id = j.optIntOrNull("sub_merchant_id"),
            transaction_id = j.optStringOrNull("transaction_id"),
            transaction_status_monitoring_done = j.optBooleanOrNull("transaction_status_monitoring_done"),
            transaction_status_monitoring_done_at = j.optStringOrNull("transaction_status_monitoring_done_at"),
            transaction_status_monitoring_status_change_detected = j.optStringOrNull("transaction_status_monitoring_status_change_detected"),
            transaction_type = j.optStringOrNull("transaction_type"),
            user_id = j.optIntOrNull("user_id"),
            vpa_app_name = j.optStringOrNull("vpa_app_name"),
            vpa_holder = j.optStringOrNull("vpa_holder"),
            vpa_id = j.optStringOrNull("vpa_id"),
            wallet_name = j.optStringOrNull("wallet_name"),
            webhook_sent = j.optBooleanOrNull("webhook_sent")
        )
    }

    private fun parseTransaction(j: JSONObject?): Transaction? {
        if (j == null) return null
        return Transaction(
            completion_time = j.optIntOrNull("completion_time"),
            transaction_type = j.optStringOrNull("transaction_type")
        )
    }

    private fun parseTransactionEnquiryOrder(j: JSONObject?): Order? {
        if (j == null) return null
        return Order(
            currency_conversion = null,
            custom_attributes = null,
            invoice_id = j.optStringOrNull("invoice_id"),
            nimbbl_order_id = j.optStringOrNull("nimbbl_order_id"),
            refund_details = null,
            status = j.optStringOrNull("status")
        )
    }

    private fun parseTransactionEnquiryResponseVo(j: JSONObject): TransactionEnquiryResponseVo {
        val txArr = j.optJSONArrayOrNull("transaction")
        val txList = txArr?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                (arr.opt(i) as? JSONObject)?.let { parseTransaction(it) }
            }
        }
        return TransactionEnquiryResponseVo(
            order = parseTransactionEnquiryOrder(j.optJSONObjectOrNull("order")),
            transaction = txList
        )
    }

    private fun parseResendOtpResponse(j: JSONObject): ResendOtpResponse =
        ResendOtpResponse(
            otp_sent = j.optBooleanOrNull("otp_sent"),
            status_code = j.optIntOrNull("status_code"),
            success = j.optBooleanOrNull("success")
        )

    private fun parseBank(j: JSONObject?): Bank? {
        if (j == null) return null
        return Bank(
            additinal_cahrges = j.optStringOrNull("additinal_cahrges"),
            bank_name = j.optStringOrNull("bank_name")
        )
    }

    private fun parseListOfBankResponse(j: JSONObject): ListOfBankResponse {
        val arr = j.optJSONArrayOrNull("bank_list")
        val list = arr?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                (a.opt(i) as? JSONObject)?.let { parseBank(it) }
            }
        } ?: emptyList()
        return ListOfBankResponse(bank_list = list)
    }

    private fun parseWallet(j: JSONObject?): Wallet? {
        if (j == null) return null
        return Wallet(
            additinal_cahrges = j.optStringOrNull("additinal_cahrges"),
            wallet_name = j.optStringOrNull("wallet_name")
        )
    }

    private fun parseListOfWalletResponse(j: JSONObject): ListOfWalletResponse {
        val arr = j.optJSONArrayOrNull("wallet_list")
        val list = arr?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                (a.opt(i) as? JSONObject)?.let { parseWallet(it) }
            }
        } ?: emptyList()
        return ListOfWalletResponse(wallet_list = list)
    }

    private fun parsePaymentModesResponseItem(j: JSONObject?): PaymentModesResponseItem? {
        if (j == null) return null
        return PaymentModesResponseItem(
            extra_info = parseExtraInfo(j.optJSONObjectOrNull("extra_info")),
            payment_mode = j.optStringOrNull("payment_mode")
        )
    }

    private fun parsePaymentModesResponse(j: JSONObject): PaymentModesResponse {
        val list = PaymentModesResponse()
        val arr = j.optJSONArray("data") ?: j.optJSONArray("payment_modes") ?: JSONArray()
        for (i in 0 until arr.length()) {
            (arr.opt(i) as? JSONObject)?.let { parsePaymentModesResponseItem(it) }?.let { list.add(it) }
        }
        return list
    }
}

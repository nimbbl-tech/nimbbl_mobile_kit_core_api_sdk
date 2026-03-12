package tech.nimbbl.coreapisdk.data.repository

import org.json.JSONObject
import tech.nimbbl.coreapisdk.api.ApiResult
import tech.nimbbl.coreapisdk.api.RawApiResponse
import tech.nimbbl.coreapisdk.api.models.responses.OrderResponse
import tech.nimbbl.coreapisdk.api.models.responses.UpdateTransactionResponse
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.api.services.CoreAppWebService
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_OrderID
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_bank
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_callback_mode
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_callback_url
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_card_detail
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_card_number
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_flow
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_invoice
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_nimbbl_consumer_message
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_nimbbl_error_code
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_nimbbl_merchant_message
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_otp
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_payment_mode
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_payment_type
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_referrer_platform
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_referrer_platform_version
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_transaction_id
import tech.nimbbl.coreapisdk.core.constants.PayloadKeys.Companion.key_upi_id
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.BASE_URL
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.CHECKOUT_CANCEL
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.TRANSACTION_ENQUIRY
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.UPDATE_ORDER
import tech.nimbbl.coreapisdk.data.models.common.CheckoutResourceVo
import tech.nimbbl.coreapisdk.data.models.common.InitiatePaymentResponse
import tech.nimbbl.coreapisdk.data.models.common.PublicKeyResponse
import tech.nimbbl.coreapisdk.data.models.common.ResendOtpResponse
import tech.nimbbl.coreapisdk.data.models.common.ResolveUserResponse
import tech.nimbbl.coreapisdk.data.models.payment.BinDataResponse
import tech.nimbbl.coreapisdk.data.models.payment.ListOfBankResponse
import tech.nimbbl.coreapisdk.data.models.payment.ListOfWalletResponse
import tech.nimbbl.coreapisdk.data.models.payment.PaymentModesResponse
import tech.nimbbl.coreapisdk.utils.JsonParser
import tech.nimbbl.coreapisdk.utils.extensions.getAPIRequestBody
import tech.nimbbl.coreapisdk.utils.extensions.getIPAddress
import tech.nimbbl.coreapisdk.utils.extensions.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.nimbbl.coreapisdk.utils.logging.ApiLoggingUtils

/*
Created by Sandeep Yadav on 20/05/24.
Copyright (c) 2024 Bigital Technologies Pvt. Ltd. All rights reserved.
*/
class NimbblRepositoryImpl(
    private val apiService: CoreAppWebService,
) : NimbblRepository {

    override suspend fun updateCheckOutCancelReason(
        token: String,
        orderId: String,
        cancelReason: String
    ): ApiResult<Unit> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put("command", "order_cancel")
        jsonObject.put("cancellation_reason", cancelReason)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.cancelCheckout(BASE_URL + CHECKOUT_CANCEL, "Bearer $token", body)
        if (raw.isSuccessful) {
            ApiResult.success(Unit, raw.code, raw.message)
        } else {
            ApiResult.error(raw.code, raw.message, raw.rawBodyString)
        }
    }

    override suspend fun getCheckOutResource(
        url: String,
        token: String,
        xNimbblKey: String
    ): ApiResult<CheckoutResourceVo> = withContext(Dispatchers.IO) {
        val raw = apiService.checkOutResource(url, xNimbblKey, "Bearer $token")
        val parsed = raw.body?.let { JsonParser.fromJson<CheckoutResourceVo>(it) }
        ApiResult.fromNullable(
            data = parsed,
            code = raw.code,
            message = raw.message,
            rawBody = raw.rawBodyString
        )
    }

    override suspend fun getListOfBanks(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String
    ): ApiResult<ListOfBankResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.getListOfBanks(url, xNimbblKey, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<ListOfBankResponse>(it) }
    }

    override suspend fun getListOfWallets(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String
    ): ApiResult<ListOfWalletResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.getListOfWallets(url, xNimbblKey, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<ListOfWalletResponse>(it) }
    }

    override suspend fun getPaymentModes(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        userToken: String
    ): ApiResult<PaymentModesResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.getPaymentModes(url, xNimbblKey, "Bearer $token", userToken, body)
        toApiResult(raw) { JsonParser.fromJson<PaymentModesResponse>(it) }
    }

    override suspend fun getOrderDetails(
        url: String,
        token: String
    ): ApiResult<OrderResponse> = withContext(Dispatchers.IO) {
        ApiLoggingUtils.logRequestDetails(
            method = "GET",
            url = url,
            customTag = "NimbblRepositoryImpl"
        )
        val raw = apiService.getOrderDetails(url, "Bearer $token")
        ApiLoggingUtils.logResponseDetails(
            code = raw.code,
            message = raw.message,
            body = raw.rawBodyString,
            customTag = "NimbblRepositoryImpl"
        )
        toApiResult(raw) { JsonParser.fromJson<OrderResponse>(it) }
    }

    override suspend fun updateOrderDetails(
        token: String,
        orderID: String,
        callback_mode: String,
        referrer_platform: String,
        referrer_platform_version: String
    ): ApiResult<OrderResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        if (callback_mode.isNotEmpty()) {
            jsonObject.put(key_callback_mode, callback_mode)
        }
        jsonObject.put(key_referrer_platform, referrer_platform)
        jsonObject.put(key_OrderID, orderID)
        jsonObject.put(key_referrer_platform_version, referrer_platform_version.ifEmpty { "2.0.0" })
        val body = getAPIRequestBody(jsonObject)
        ApiLoggingUtils.logRequestDetails(
            method = "PATCH",
            url = BASE_URL + UPDATE_ORDER,
            body = jsonObject.toString(),
            customTag = "NimbblRepositoryImpl"
        )
        val raw = apiService.updateOrder(BASE_URL + UPDATE_ORDER, "Bearer $token", body)
        ApiLoggingUtils.logResponseDetails(
            code = raw.code,
            message = raw.message,
            body = raw.rawBodyString,
            customTag = "NimbblRepositoryImpl"
        )
        toApiResult(raw) { JsonParser.fromJson<OrderResponse>(it) }
    }

    override suspend fun resolveUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        deviceVerified: Boolean?,
        orderId: String
    ): ApiResult<ResolveUserResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(PayloadKeys.key_mobileNumber, mobileNumber)
        jsonObject.put(PayloadKeys.key_deviceVerified, deviceVerified)
        jsonObject.put(PayloadKeys.key_userAgent, "")
        jsonObject.put(PayloadKeys.key_ipAddress, getIPAddress(true))
        jsonObject.put(PayloadKeys.key_fingerPrint, md5("" + ""))
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.resolveUser(url, xNimbblKey, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<ResolveUserResponse>(it) }
    }

    override suspend fun verifyUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        otp: String,
        orderId: String
    ): ApiResult<ResolveUserResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(PayloadKeys.key_mobileNumber, mobileNumber)
        jsonObject.put(key_otp, otp)
        jsonObject.put(PayloadKeys.key_userAgent, "")
        jsonObject.put(PayloadKeys.key_ipAddress, getIPAddress(true))
        jsonObject.put(PayloadKeys.key_fingerPrint, md5("" + ""))
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.verifyUser(url, xNimbblKey, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<ResolveUserResponse>(it) }
    }

    override suspend fun initiatePayment(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        callbackUrl: String?,
        paymentMode: String,
        subPaymentMode: String?,
        cardDetailJsonObj: String?,
        upiId: String?
    ): ApiResult<InitiatePaymentResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_payment_mode, paymentMode)
        if (subPaymentMode != null && subPaymentMode.isNotEmpty()) {
            jsonObject.put(key_bank, subPaymentMode)
        }
        if (callbackUrl != null && callbackUrl.isNotEmpty()) {
            jsonObject.put(key_callback_url, callbackUrl)
        }
        if (cardDetailJsonObj?.isNotEmpty() == true) {
            jsonObject.put(key_card_detail, cardDetailJsonObj)
        }
        if (upiId != null && upiId.isNotEmpty()) {
            jsonObject.put(key_upi_id, upiId)
        }
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.initiatePayment(url, xNimbblKey, "Bearer $token", "", body)
        toApiResult(raw) { JsonParser.fromJson<InitiatePaymentResponse>(it) }
    }

    override suspend fun makePayment(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        paymentMode: String,
        paymentType: String,
        otp: String,
        upiId: String,
        flow: String,
        transactionId: String
    ): ApiResult<InitiatePaymentResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_payment_mode, paymentMode)
        jsonObject.put(key_payment_type, paymentType)
        jsonObject.put(key_otp, otp)
        jsonObject.put(key_upi_id, upiId)
        jsonObject.put(key_flow, flow)
        jsonObject.put(key_transaction_id, transactionId)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.makePayment(url, xNimbblKey, "Bearer $token", "", body)
        toApiResult(raw) { JsonParser.fromJson<InitiatePaymentResponse>(it) }
    }

    override suspend fun getPublicKey(url: String): ApiResult<PublicKeyResponse> = withContext(Dispatchers.IO) {
        val raw = apiService.getPublicKey(url)
        toApiResult(raw) { JsonParser.fromJson<PublicKeyResponse>(it) }
    }

    override suspend fun getBinData(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        cardNo: String
    ): ApiResult<BinDataResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_card_number, cardNo.replace(" ", ""))
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.getBinData(url, xNimbblKey, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<BinDataResponse>(it) }
    }

    override suspend fun updateTransactionDetail(
        url: String,
        token: String,
        transactionId: String,
        errorCode: String,
        consumerMessage: String,
        merchantMessage: String
    ): ApiResult<UpdateTransactionResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_nimbbl_error_code, errorCode)
        jsonObject.put(key_nimbbl_consumer_message, consumerMessage)
        jsonObject.put(key_nimbbl_merchant_message, merchantMessage)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.updateTransaction(url, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<UpdateTransactionResponse>(it) }
    }

    override suspend fun getTransactionEnquiry(
        token: String,
        orderId: String,
        invoiceId: String,
        transactionId: String
    ): ApiResult<TransactionEnquiryResponseVo> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_invoice, invoiceId)
        jsonObject.put(key_transaction_id, transactionId)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.getTransactionEnquiry(
            BASE_URL + TRANSACTION_ENQUIRY,
            "Bearer $token",
            body
        )
        toApiResult(raw) { JsonParser.fromJson<TransactionEnquiryResponseVo>(it) }
    }

    override suspend fun resendOtp(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        paymentMode: String,
        transactionId: String
    ): ApiResult<ResendOtpResponse> = withContext(Dispatchers.IO) {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_payment_mode, paymentMode)
        jsonObject.put(key_transaction_id, transactionId)
        val body = getAPIRequestBody(jsonObject)
        val raw = apiService.resendOtp(url, xNimbblKey, "Bearer $token", body)
        toApiResult(raw) { JsonParser.fromJson<ResendOtpResponse>(it) }
    }

    override fun setSubMerchantId(subMerchantId: String) {}
    override fun getSubMerchantId(): String = ""
    override fun setMerchantPackageName(packageName: String) {}
    override fun getSubMerchantPackageName(): String = ""

    private fun <T> toApiResult(raw: RawApiResponse, parse: (JSONObject) -> T?): ApiResult<T> {
        val data = raw.body?.let { parse(it) }
        return ApiResult.fromNullable(
            data = data,
            code = raw.code,
            message = raw.message,
            rawBody = raw.rawBodyString
        )
    }
}

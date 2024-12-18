package tech.nimbbl.coreapisdk.repository

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response
import tech.nimbbl.coreapisdk.api.CoreAppWebService
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.BASE_URL
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.CHECKOUT_CANCEL
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.CHECKOUT_DETAILS
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.TRANSACTION_ENQUIRY
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.UPDATE_ORDER
import tech.nimbbl.coreapisdk.models.BinDataResponse
import tech.nimbbl.coreapisdk.models.CheckoutResourceVo
import tech.nimbbl.coreapisdk.models.InitiatePaymentResponse
import tech.nimbbl.coreapisdk.models.ListOfBankResponse
import tech.nimbbl.coreapisdk.models.ListOfWalletResponse
import tech.nimbbl.coreapisdk.models.OrderResponse
import tech.nimbbl.coreapisdk.models.PaymentModesResponse
import tech.nimbbl.coreapisdk.models.PublicKeyResponse
import tech.nimbbl.coreapisdk.models.ResendOtpResponse
import tech.nimbbl.coreapisdk.models.ResolveUserResponse
import tech.nimbbl.coreapisdk.models.UpdateTransactionResponse
import tech.nimbbl.coreapisdk.models.checkout_details.CheckoutDetailsResponseVo
import tech.nimbbl.coreapisdk.models.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.utils.PayloadKeys
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_OrderID
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_bank
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_callback_mode
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_callback_url
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_card_detail
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_card_number
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_flow
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_invoice
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_nimbbl_consumer_message
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_nimbbl_error_code
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_nimbbl_merchant_message
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_otp
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_payment_mode
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_payment_type
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_referrer_platform
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_referrer_platform_version
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_transaction_id
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_upi_id
import tech.nimbbl.coreapisdk.utils.getAPIRequestBody
import tech.nimbbl.coreapisdk.utils.getIPAddress
import tech.nimbbl.coreapisdk.utils.md5
import tech.nimbbl.coreapisdk.utils.printLog
import tech.nimbbl.coreapisdk.utils.writeResponseBodyToDisk


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
    ): Response<Void> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put("command", "order_cancel")
        jsonObject.put("cancellation_reason", cancelReason)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.cancelCheckout(BASE_URL + CHECKOUT_CANCEL, "Bearer $token", body)
    }

    override suspend fun getCheckOutResource(
        url: String,
        token: String,
        xNimbblKey: String
    ): Response<CheckoutResourceVo> {

        val response = apiService.checkOutResource(url, xNimbblKey, "Bearer $token")
        val gson = Gson()
        val jsonStr: String = gson.toJson(response.body() ?: "")
        printLog("SAN", jsonStr)
        if (response.body() != null && response.body()?.data != null && response.body()?.data!!.isNotEmpty()) {
            for (data in response.body()?.data!!) {
                for (items in data.items!!) {
                    val code = items.app_code ?: items.sub_payment_code
                    code?.let { items.logo_url = downloadAndSaveLogo(items.logo_url, it) }
                    if (items.items is List<*>) {
                        for (item in items.items) {
                            val logoUrl = (item as LinkedTreeMap<*, *>)["logo_url"].toString()
                            val filename = item["sub_payment_code"].toString()
                            downloadAndSaveLogo(logoUrl, filename)
                        }
                    }
                    if (items.items is LinkedTreeMap<*, *>) {
                        val schemeobj = items.items["schemes"] as ArrayList<*>
                        for (obj in schemeobj) {
                            val logoUrl = (obj as LinkedTreeMap<*, *>)["logo_url"].toString()
                            val fileName = obj["scheme_code"].toString()
                            downloadAndSaveLogo(logoUrl, fileName)
                        }
                    }


                }

            }
        }
        return response
    }

    override suspend fun getListOfBanks(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String
    ): Response<ListOfBankResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        val listOfBanksResponse = apiService.getListOfBanks(url, xNimbblKey, "Bearer $token", body)
        return listOfBanksResponse
    }

    override suspend fun getListOfWallets(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String
    ): Response<ListOfWalletResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        val listOfWalletResponse =
            apiService.getListOfWallets(url, xNimbblKey, "Bearer $token", body)
        return listOfWalletResponse
    }

    override suspend fun getPaymentModes(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        userToken: String
    ): Response<PaymentModesResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        val paymentModesResponse =
            apiService.getPaymentModes(url, xNimbblKey, "Bearer $token", userToken, body)
        return paymentModesResponse
    }

    override suspend fun getOrderDetails(
        url: String,
        token: String
    ): Response<OrderResponse> {
        return apiService.getOrderDetails(url, "Bearer $token")
    }

    override suspend fun updateOrderDetails(
        token: String,
        orderId: String,
        callback_mode: String,
        referrer_platform: String,
        referrer_platform_version: String
    ): Response<OrderResponse> {
        val jsonObject = JSONObject()
        if (callback_mode.isNotEmpty()) {
            jsonObject.put(key_callback_mode, callback_mode)
        }
        jsonObject.put(key_referrer_platform, referrer_platform)
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_referrer_platform_version, referrer_platform_version)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.updateOrder(BASE_URL + UPDATE_ORDER, "Bearer $token", body)
    }

    override suspend fun resolveUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        deviceVerified: Boolean?,
        orderId: String
    ): Response<ResolveUserResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(PayloadKeys.key_mobileNumber, mobileNumber)
        jsonObject.put(PayloadKeys.key_deviceVerified, deviceVerified)
        jsonObject.put(PayloadKeys.key_userAgent, "")
        jsonObject.put(PayloadKeys.key_ipAddress, getIPAddress(true))
        jsonObject.put(
            PayloadKeys.key_fingerPrint,
            md5("" + "")
        )
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.resolveUser(url, xNimbblKey, "Bearer $token", body)
    }

    override suspend fun verifyUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        otp: String,
        orderId: String
    ): Response<ResolveUserResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(PayloadKeys.key_mobileNumber, mobileNumber)
        jsonObject.put(key_otp, otp)
        jsonObject.put(PayloadKeys.key_userAgent, "")
        jsonObject.put(PayloadKeys.key_ipAddress, getIPAddress(true))
        jsonObject.put(
            PayloadKeys.key_fingerPrint,
            md5("" + "")
        )
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.verifyUser(url, xNimbblKey, "Bearer $token", body)
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
    ): Response<InitiatePaymentResponse> {
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
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.initiatePayment(
            url,
            xNimbblKey,
            "Bearer $token",
            "",
            body
        )
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
    ): Response<InitiatePaymentResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_payment_mode, paymentMode)
        jsonObject.put(key_payment_type, paymentType)
        jsonObject.put(key_otp, otp)
        jsonObject.put(key_upi_id, upiId)
        jsonObject.put(key_flow, flow)
        jsonObject.put(key_transaction_id, transactionId)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.makePayment(
            url,
            xNimbblKey,
            "Bearer $token",
            "",
            body
        )
    }

    override suspend fun getPublicKey(url: String): Response<PublicKeyResponse> {
        return apiService.getPublicKey(url)
    }

    override suspend fun getBinData(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        cardNo: String
    ): Response<BinDataResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_card_number, cardNo.replace(" ", ""))
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.getBinData(url, xNimbblKey, "Bearer $token", body)
    }

    override suspend fun updateTransactionDetail(
        url: String,
        token: String,
        transactionId: String,
        errorCode: String,
        consumerMessage: String,
        merchantMessage: String
    ): Response<UpdateTransactionResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_nimbbl_error_code, errorCode)
        jsonObject.put(key_nimbbl_consumer_message, consumerMessage)
        jsonObject.put(key_nimbbl_merchant_message, merchantMessage)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.updateTransaction(url, "Bearer $token", body)
    }

    override suspend fun getTransactionEnquiry(
        token: String,
        orderId: String,
        invoiceId: String,
        transactionId: String
    ): Response<TransactionEnquiryResponseVo> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_invoice, invoiceId)
        jsonObject.put(key_transaction_id, transactionId)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.getTransactionEnquiry(
            BASE_URL + TRANSACTION_ENQUIRY,
            "Bearer $token",
            body
        )
    }

    override suspend fun resendOtp(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        paymentMode: String,
        transactionId: String
    ): Response<ResendOtpResponse> {
        val jsonObject = JSONObject()
        jsonObject.put(key_OrderID, orderId)
        jsonObject.put(key_payment_mode, paymentMode)
        jsonObject.put(key_transaction_id, transactionId)
        val body: RequestBody = getAPIRequestBody(jsonObject)
        return apiService.resendOtp(url, xNimbblKey, "Bearer $token", body)
    }

    override fun setSubMerchantId(subMerchantId: String) {
    }

    override fun getSubMerchantId(): String {
        return ""
    }

    override fun setMerchantPackageName(packageName: String) {
    }

    override fun getSubMerchantPackageName(): String {
        return ""
    }

    override suspend fun getCheckoutDetails(token: String): Response<CheckoutDetailsResponseVo>? {
        return apiService.getCheckoutDetails(
            BASE_URL + CHECKOUT_DETAILS,
            "Bearer $token",
        )
    }


    private suspend fun downloadAndSaveLogo(
        logoUrl: String?,
        fileName: String,
    ): String {
        return if (logoUrl != null && logoUrl.isNotEmpty() && !logoUrl.equals("null", true)) {
            val responseBody = apiService.downloadBankLogo(logoUrl)
            val fileExtension = logoUrl.substring(logoUrl.lastIndexOf("."));
            val savedFilePath =
                writeResponseBodyToDisk("", responseBody.body(), fileName + fileExtension)
            if (savedFilePath.isNotEmpty()) {
                savedFilePath
            } else {
                ""
            }
        } else {
            ""
        }
    }


}
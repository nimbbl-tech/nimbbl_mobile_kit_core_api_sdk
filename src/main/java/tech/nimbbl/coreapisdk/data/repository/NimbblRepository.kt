package tech.nimbbl.coreapisdk.data.repository

import tech.nimbbl.coreapisdk.api.ApiResult
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

/*
Created by Sandeep Yadav on 22/05/24.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

interface NimbblRepository {

    suspend fun updateCheckOutCancelReason(
        token: String, orderId: String, cancelReason: String
    ): ApiResult<Unit>

    suspend fun getCheckOutResource(
        url: String, token: String, xNimbblKey: String
    ): ApiResult<CheckoutResourceVo>

    suspend fun getListOfBanks(
        url: String, token: String, xNimbblKey: String, orderId: String
    ): ApiResult<ListOfBankResponse>

    suspend fun getListOfWallets(
        url: String, token: String, xNimbblKey: String, orderId: String
    ): ApiResult<ListOfWalletResponse>

    suspend fun getPaymentModes(
        url: String, token: String, xNimbblKey: String, orderId: String, userToken: String
    ): ApiResult<PaymentModesResponse>

    suspend fun getOrderDetails(
        url: String, token: String
    ): ApiResult<OrderResponse>

    suspend fun updateOrderDetails(
        token: String,
        orderID: String,
        callback_mode: String,
        referrer_platform: String,
        referrer_platform_version: String
    ): ApiResult<OrderResponse>

    suspend fun resolveUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        deviceVerified: Boolean?,
        orderId: String
    ): ApiResult<ResolveUserResponse>

    suspend fun verifyUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        otp: String,
        orderId: String
    ): ApiResult<ResolveUserResponse>

    suspend fun initiatePayment(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        callbackUrl: String?,
        paymentMode: String,
        subPaymentMode: String?,
        cardDetailJsonObj: String?,
        upiId: String?
    ): ApiResult<InitiatePaymentResponse>

    suspend fun makePayment(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        paymentMode: String,
        paymentType: String,
        otp: String,
        upiId: String,
        flow: String,
        transactionId: String,
    ): ApiResult<InitiatePaymentResponse>

    suspend fun getPublicKey(url: String): ApiResult<PublicKeyResponse>

    suspend fun getBinData(
        url: String, token: String, xNimbblKey: String, orderId: String, cardNo: String
    ): ApiResult<BinDataResponse>

    suspend fun updateTransactionDetail(
        url: String,
        token: String,
        transactionId: String,
        errorCode: String,
        consumerMessage: String,
        merchantMessage: String
    ): ApiResult<UpdateTransactionResponse>

    suspend fun getTransactionEnquiry(
        token: String, orderId: String, invoiceId: String, transactionId: String
    ): ApiResult<TransactionEnquiryResponseVo>

    suspend fun resendOtp(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        paymentMode: String,
        transactionId: String
    ): ApiResult<ResendOtpResponse>

    fun setSubMerchantId(subMerchantId: String)
    fun getSubMerchantId(): String
    fun setMerchantPackageName(packageName: String)
    fun getSubMerchantPackageName(): String
}

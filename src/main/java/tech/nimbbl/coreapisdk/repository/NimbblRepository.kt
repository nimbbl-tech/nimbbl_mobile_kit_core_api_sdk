package tech.nimbbl.coreapisdk.repository

import retrofit2.Response
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

/*
Created by Sandeep Yadav on 22/05/24.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

interface NimbblRepository {

    suspend fun updateCheckOutCancelReason(
        token: String, orderId: String, cancelReason: String
    ): Response<Void>


    suspend fun getCheckOutResource(
        url: String, token: String, xNimbblKey: String
    ): Response<CheckoutResourceVo>

    suspend fun getListOfBanks(
        url: String, token: String, xNimbblKey: String, orderId: String
    ): Response<ListOfBankResponse>

    suspend fun getListOfWallets(
        url: String, token: String, xNimbblKey: String, orderId: String
    ): Response<ListOfWalletResponse>

    suspend fun getPaymentModes(
        url: String, token: String, xNimbblKey: String, orderId: String, userToken: String
    ): Response<PaymentModesResponse>

    suspend fun getOrderDetails(
        url: String, token: String
    ): Response<OrderResponse>

    suspend fun updateOrderDetails(
        token: String,
        orderID: String,
        callback_mode: String,
        referrer_platform: String,
        referrer_platform_version: String
    ): Response<OrderResponse>

    suspend fun resolveUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        deviceVerified: Boolean?,
        orderId: String
    ): Response<ResolveUserResponse>

    suspend fun verifyUser(
        url: String,
        token: String,
        xNimbblKey: String,
        mobileNumber: String,
        otp: String,
        orderId: String
    ): Response<ResolveUserResponse>


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

    ): Response<InitiatePaymentResponse>

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
    ): Response<InitiatePaymentResponse>

    suspend fun getPublicKey(url: String): Response<PublicKeyResponse>

    suspend fun getBinData(
        url: String, token: String, xNimbblKey: String, orderId: String, cardNo: String
    ): Response<BinDataResponse>

    suspend fun updateTransactionDetail(
        url: String,
        token: String,
        transactionId: String,
        errorCode: String,
        consumerMessage: String,
        merchantMessage: String
    ): Response<UpdateTransactionResponse>

    suspend fun getTransactionEnquiry(
        token: String, orderId: String, invoiceId: String, transactionId: String
    ): Response<TransactionEnquiryResponseVo>

    suspend fun resendOtp(
        url: String,
        token: String,
        xNimbblKey: String,
        orderId: String,
        paymentMode: String,
        transactionId: String
    ): Response<ResendOtpResponse>

    fun setSubMerchantId(subMerchantId: String)
    fun getSubMerchantId(): String
    fun setMerchantPackageName(packageName: String)
    fun getSubMerchantPackageName(): String
    suspend fun getCheckoutDetails(token: String): Response<CheckoutDetailsResponseVo>?


}

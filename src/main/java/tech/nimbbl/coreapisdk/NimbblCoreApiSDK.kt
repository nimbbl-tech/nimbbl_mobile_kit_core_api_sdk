package tech.nimbbl.coreapisdk

import retrofit2.Response
import tech.nimbbl.coreapisdk.api.CoreAppWebService
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.BASE_URL
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.DEVICE_FINGERPRINT
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.FINGERPRINT
import tech.nimbbl.coreapisdk.interfaces.NimbblPayNativeCheckoutPaymentListener
import tech.nimbbl.coreapisdk.models.OrderResponse
import tech.nimbbl.coreapisdk.models.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.repository.NimbblRepository
import tech.nimbbl.coreapisdk.repository.NimbblRepositoryImpl
import tech.nimbbl.coreapisdk.utils.getIPAddress


class NimbblCoreApiSDK private constructor() {
    private var nimbblPayListener: NimbblPayNativeCheckoutPaymentListener? = null

    fun initialiseAPISDK(url: String, fingerPrint: String, deviceFingerPrint: String) {
        BASE_URL = url
        FINGERPRINT = fingerPrint
        DEVICE_FINGERPRINT = deviceFingerPrint
    }

    suspend fun updateCheckOutCancelReason(token: String, orderId: String, cancelReason: String) {
        getAPIRepositoryInstance()?.updateCheckOutCancelReason(token,orderId,cancelReason)
    }

    suspend fun getTransactionEnquiry(token: String, orderId: String, invoiceId: String,transactionId: String) : Response<TransactionEnquiryResponseVo>? {
        return getAPIRepositoryInstance()?.getTransactionEnquiry(token,orderId,invoiceId,transactionId)
    }

    suspend fun updateOrder(token: String, orderId: String, callbackMode: String,platformType: String) : Response<OrderResponse>? {
        return getAPIRepositoryInstance()?.updateOrderDetails(token,orderId,callbackMode,platformType,"2.0.0")
    }


    companion object {
        private var instance: NimbblCoreApiSDK? = null
        private var nimbblApiRepository: NimbblRepository? = null

        fun getInstance(): NimbblCoreApiSDK? {
            if (instance == null) {

                instance = NimbblCoreApiSDK()
            }
            return instance
        }


        fun getAPIRepositoryInstance(): NimbblRepository? {
            if (nimbblApiRepository == null) {

                nimbblApiRepository =
                    NimbblRepositoryImpl(
                        CoreAppWebService(
                            BASE_URL, DEVICE_FINGERPRINT, FINGERPRINT, getIPAddress(true)
                        )!!,
                        )
            }
            return nimbblApiRepository
        }
    }
}
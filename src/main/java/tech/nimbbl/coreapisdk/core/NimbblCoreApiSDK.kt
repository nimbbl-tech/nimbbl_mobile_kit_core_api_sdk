package tech.nimbbl.coreapisdk.core

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import android.util.Log
import tech.nimbbl.coreapisdk.api.ApiResult
import tech.nimbbl.coreapisdk.api.RestApiUtils
import tech.nimbbl.coreapisdk.api.models.responses.OrderResponse
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.api.services.CoreAppWebService
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.core.constants.Constants.sdk_version
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.BASE_URL
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.DEVICE_FINGERPRINT
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.FINGERPRINT
import tech.nimbbl.coreapisdk.data.repository.NimbblRepository
import tech.nimbbl.coreapisdk.data.repository.NimbblRepositoryImpl
import tech.nimbbl.coreapisdk.utils.LoggingConfig
import tech.nimbbl.coreapisdk.utils.extensions.getIPAddress
import tech.nimbbl.coreapisdk.utils.logging.EventLoggingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NimbblCoreApiSDK private constructor() {

    fun initialiseAPISDK(url: String, fingerPrint: String, deviceFingerPrint: String, appCode: String? = null) {
        BASE_URL = url
        RestApiUtils.NIMBBL_TECH_URL = url
        FINGERPRINT = fingerPrint
        DEVICE_FINGERPRINT = deviceFingerPrint
        
        // Configure logging based on build type
        if (is_debug_enabled) {
            LoggingConfig.configureForDevelopment()
        } else {
            LoggingConfig.configureForProduction()
        }
        
        // Event logging is always enabled (events always sent to server)
        EventLoggingService.setEventLoggingEnabled(true)
        
        // Store the app code for logging
        if (!appCode.isNullOrEmpty()) {
            EventLoggingService.setAppCode(appCode)
        }

        // Initialize repository on IO (non-blocking; lazy init will create if checkout runs first)
        initRepositoryAsync()
    }

    private fun initRepositoryAsync() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val ip = getIPAddress(true)
                val webService = CoreAppWebService(
                    BASE_URL,
                    DEVICE_FINGERPRINT,
                    FINGERPRINT,
                    ip
                )
                if (webService != null) {
                    synchronized(Companion.repoLock) {
                        if (nimbblApiRepository == null) {
                            nimbblApiRepository = NimbblRepositoryImpl(webService)
                            if (is_debug_enabled) Log.d(TAG, MSG_SDK_INIT_SUCCESS)
                        }
                    }
                } else {
                    Log.e(TAG, MSG_WEB_SERVICE_INIT_FAILED)
                }
            } catch (e: Exception) {
                Log.e(TAG, MSG_SDK_INIT_ERROR.format(e.message), e)
            }
        }
    }


    suspend fun getTransactionEnquiry(
        token: String,
        orderId: String,
        invoiceId: String,
        transactionId: String
    ): ApiResult<TransactionEnquiryResponseVo>? {
        return getAPIRepositoryInstance()?.getTransactionEnquiry(
            token,
            orderId,
            invoiceId,
            transactionId
        )
    }

    suspend fun updateOrder(
        token: String,
        orderId: String,
        callbackMode: String,
        platformType: String
    ): ApiResult<OrderResponse>? {
        return getAPIRepositoryInstance()?.updateOrderDetails(
            token, orderId, callbackMode, platformType,
            sdk_version
        )
    }

    // Event Logging Methods

    /**
     * Log an event using the core API SDK event logging service
     */
    fun logEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null,
        customUserAgent: String? = null,
        customDeviceInfo: Map<String, String>? = null,
        customAppInfo: Map<String, String>? = null
    ) {
        EventLoggingService.getInstance().logEvent(
            context,
            eventName,
            orderId,
            token,
            subMerchantId,
            additionalData,
            customUserAgent,
            customDeviceInfo,
            customAppInfo
        )
    }


    companion object {
        private const val TAG = "NimbblCoreApiSDK"
        internal val repoLock = Any()

        // Log Messages
        private const val MSG_SDK_INIT_SUCCESS = "SDK initialized successfully with repository"
        private const val MSG_WEB_SERVICE_INIT_FAILED = "Failed to create web service during initialization"
        private const val MSG_SDK_INIT_ERROR = "Error during SDK initialization: %s"
        private const val MSG_REPO_INIT_SUCCESS = "Repository initialized successfully"
        private const val MSG_WEB_SERVICE_FAILED = "Failed to create web service"
        private const val MSG_REPO_INIT_ERROR = "Error initializing repository: %s"

        @Volatile
        private var instance: NimbblCoreApiSDK? = null

        // ...existing code...
        private var nimbblApiRepository: NimbblRepository? = null

        /**
         * Get singleton instance of NimbblCoreApiSDK
         * Thread-safe implementation using double-checked locking
         */
        fun getInstance(): NimbblCoreApiSDK {
            return instance ?: synchronized(this) {
                instance ?: NimbblCoreApiSDK().also { instance = it }
            }
        }


        /**
         * Get API repository instance with null safety.
         * Lazy init runs on Dispatchers.IO to avoid blocking the caller thread.
         */
        suspend fun getAPIRepositoryInstance(): NimbblRepository? {
            nimbblApiRepository?.let { return it }
            if (BASE_URL.isBlank()) {
                Log.e(TAG, "SDK not initialized: call initialiseAPISDK before checkout")
                return null
            }
            val newRepo = withContext(Dispatchers.IO) {
                try {
                    val webService = CoreAppWebService(
                        BASE_URL,
                        DEVICE_FINGERPRINT,
                        FINGERPRINT,
                        getIPAddress(true)
                    )
                    if (webService != null) {
                        if (is_debug_enabled) Log.d(TAG, MSG_REPO_INIT_SUCCESS)
                        NimbblRepositoryImpl(webService)
                    } else {
                        Log.e(TAG, MSG_WEB_SERVICE_FAILED)
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, MSG_REPO_INIT_ERROR.format(e.message), e)
                    null
                }
            } ?: return null
            return synchronized(repoLock) {
                nimbblApiRepository ?: newRepo.also { nimbblApiRepository = it }
            }
        }
    }
}
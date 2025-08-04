package tech.nimbbl.coreapisdk.core

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.nimbbl.coreapisdk.api.models.responses.CreateOrderResponse
import tech.nimbbl.coreapisdk.api.models.responses.OrderResponse
import tech.nimbbl.coreapisdk.api.models.responses.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.api.services.CoreAppWebService
import tech.nimbbl.coreapisdk.api.services.OrderCreationService
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.BASE_URL
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.DEVICE_FINGERPRINT
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants.Companion.FINGERPRINT
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled
import tech.nimbbl.coreapisdk.data.repository.NimbblRepository
import tech.nimbbl.coreapisdk.data.repository.NimbblRepositoryImpl
import tech.nimbbl.coreapisdk.interfaces.checkout.NimbblPayNativeCheckoutPaymentListener
import tech.nimbbl.coreapisdk.utils.extensions.getIPAddress
import tech.nimbbl.coreapisdk.utils.logging.EventLoggingService
import tech.nimbbl.coreapisdk.utils.payloads.OrderCreationPayload
import android.util.Log
import tech.nimbbl.coreapisdk.core.constants.Constants.SDK_VERSION_CENTRAL
import tech.nimbbl.coreapisdk.core.constants.Constants.sdk_version
import java.io.IOException


class NimbblCoreApiSDK private constructor() {
    private var nimbblPayListener: NimbblPayNativeCheckoutPaymentListener? = null

    fun initialiseAPISDK(url: String, fingerPrint: String, deviceFingerPrint: String) {
        BASE_URL = url
        FINGERPRINT = fingerPrint
        DEVICE_FINGERPRINT = deviceFingerPrint
        
        // Initialize repository after setting up the configuration
        try {
            val webService = CoreAppWebService(
                BASE_URL, 
                DEVICE_FINGERPRINT, 
                FINGERPRINT, 
                getIPAddress(true)
            )
            
            if (webService != null) {
                nimbblApiRepository = NimbblRepositoryImpl(webService)
                if (is_debug_enabled) {
                    Log.d("NimbblCoreApiSDK", "SDK initialized successfully with repository")
                }
            } else {
                Log.e("NimbblCoreApiSDK", "Failed to create web service during initialization")
            }
        } catch (e: Exception) {
            Log.e("NimbblCoreApiSDK", "Error during SDK initialization: ${e.message}", e)
        }
    }

    suspend fun updateCheckOutCancelReason(token: String, orderId: String, cancelReason: String) {
        getAPIRepositoryInstance()?.updateCheckOutCancelReason(token,orderId,cancelReason)
    }

    suspend fun getTransactionEnquiry(token: String, orderId: String, invoiceId: String,transactionId: String) : Response<TransactionEnquiryResponseVo>? {
        return getAPIRepositoryInstance()?.getTransactionEnquiry(token,orderId,invoiceId,transactionId)
    }

    suspend fun updateOrder(token: String, orderId: String, callbackMode: String,platformType: String) : Response<OrderResponse>? {
        return getAPIRepositoryInstance()?.updateOrderDetails(token,orderId,callbackMode,platformType,
            sdk_version)
    }



    // Order Creation Methods
    
    /**
     * Create a new order (iOS-compatible implementation)
     * @param shopBaseUrl The shop base URL for order creation
     * @param totalAmount The total amount for the order
     * @param emailId Customer email ID
     * @param firstName Customer first name
     * @param mobileNumber Customer mobile number
     * @param productId Product ID
     * @param paymentMode Payment mode (optional, defaults to "All")
     * @param subPaymentMode Sub payment mode (optional)
     * @return Response containing the created order details
     */
    suspend fun createOrder(
        shopBaseUrl: String,
        totalAmount: Int,
        emailId: String,
        firstName: String,
        mobileNumber: String,
        productId: String,
        paymentMode: String = "All",
        subPaymentMode: String? = null
    ): Response<CreateOrderResponse>? {
        try {
            // Validate input parameters
            if (shopBaseUrl.isNullOrEmpty()) {
                Log.e("NimbblCoreApiSDK", "createOrder: shopBaseUrl is null or empty")
                return null
            }
            
            if (totalAmount <= 0) {
                Log.e("NimbblCoreApiSDK", "createOrder: totalAmount must be greater than 0")
                return null
            }
            
            // Use shop order creation URL (matching iOS implementation)
            val orderCreationUrl = getShopOrderUrl(shopBaseUrl)
            
            // Handle empty payment mode by defaulting to "All"
            val finalPaymentMode = if (paymentMode.isNullOrEmpty()) "All" else paymentMode
            
            val request = OrderCreationPayload.createShopOrderRequest(
                currency = "INR",
                amount = totalAmount.toString(),
                productId = productId,
                orderLineItems = true,
                checkoutExperience = "redirect",
                paymentMode = finalPaymentMode,
                subPaymentMode = subPaymentMode,
                userEmail = emailId,
                userName = firstName,
                userMobileNumber = mobileNumber
            )
            
            if (is_debug_enabled) {
                android.util.Log.d("NimbblCoreApiSDK", "Creating shop order with URL: $orderCreationUrl")
                android.util.Log.d("NimbblCoreApiSDK", "Shop order request: $request")
            }
            
            // Create a dynamic Retrofit instance for this specific call
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(shopBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            val orderService = retrofit.create(OrderCreationService::class.java)
            val response = orderService.createOrder(orderCreationUrl, request)
            
            if (response.isSuccessful) {
                if (is_debug_enabled) {
                    android.util.Log.d("NimbblCoreApiSDK", "Shop order created successfully: ${response.body()}")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                if (is_debug_enabled) {
                    android.util.Log.e("NimbblCoreApiSDK", "Shop order creation failed. Status: ${response.code()}, Error: $errorBody")
                }
            }
            
            return response
        } catch (e: IOException) {
            Log.e("NimbblCoreApiSDK", "Network error during shop order creation: ${e.message}", e)
            return null
        } catch (e: IllegalArgumentException) {
            Log.e("NimbblCoreApiSDK", "Invalid argument during shop order creation: ${e.message}", e)
            return null
        } catch (e: Exception) {
            Log.e("NimbblCoreApiSDK", "Unexpected error during shop order creation: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Get shop order URL based on environment (matching iOS implementation)
     * @param baseUrl The base URL
     * @return The shop order creation URL
     */
    private fun getShopOrderUrl(baseUrl: String): String {
        return when {
            baseUrl.contains("qa") -> {
                // For QA environments, replace 'api' with 'sonicshopapi'
                val shopHost = baseUrl.replace("api", "sonicshopapi")
                // Ensure proper URL construction without double slashes
                if (shopHost.endsWith("/")) {
                    "${shopHost}create-shop"
                } else {
                    "$shopHost/create-shop"
                }
            }
            baseUrl.contains("pp") -> {
                // For pre-production
                "https://sonicshopapipp.nimbbl.tech/create-shop"
            }
            baseUrl.contains("api.nimbbl.tech") && !baseUrl.contains("qa") && !baseUrl.contains("pp") -> {
                // For production
                "https://sonicshopapi.nimbbl.tech/create-shop"
            }
            else -> {
                // Fallback to production
                "https://sonicshopapi.nimbbl.tech/create-shop"
            }
        }
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
    
    /**
     * Log a checkout event
     */
    fun logCheckoutEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null
    ) {
        logEvent(context, eventName, orderId, token,subMerchantId, additionalData)
    }
    
    /**
     * Log a payment event
     */
    fun logPaymentEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String?,
        additionalData: Map<String, Any>? = null
    ) {
        logEvent(context, eventName, orderId, token,subMerchantId, additionalData)
    }
    
    /**
     * Log an API call event
     */
    fun logApiEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null
    ) {
        logEvent(context, eventName, orderId, token,subMerchantId, additionalData)
    }
    
    /**
     * Log a transaction event
     */
    fun logTransactionEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null
    ) {
        logEvent(context, eventName, orderId, token,subMerchantId, additionalData)
    }
    
    /**
     * Log a user event
     */
    fun logUserEvent(
        context: Context,
        eventName: String,
        orderId: String? = null,
        token: String? = null,
        subMerchantId: String? = null,
        additionalData: Map<String, Any>? = null
    ) {
        logEvent(context, eventName, orderId, token,subMerchantId, additionalData)
    }


    companion object {
        private var instance: NimbblCoreApiSDK? = null
        private var nimbblApiRepository: NimbblRepository? = null
        private var orderCreationService: OrderCreationService? = null

        fun getInstance(): NimbblCoreApiSDK? {
            if (instance == null) {

                instance = NimbblCoreApiSDK()
            }
            return instance
        }

        /**
         * Create a new order (simplified interface)
         * @param shopBaseUrl The shop base URL for order creation
         * @param totalAmount The total amount for the order
         * @param emailId Customer email ID
         * @param firstName Customer first name
         * @param mobileNumber Customer mobile number
         * @param productId Product ID
         * @param paymentMode Payment mode (optional, defaults to "All")
         * @param subPaymentMode Sub payment mode (optional)
         * @return Response containing the created order details
         */
        suspend fun createOrder(
            shopBaseUrl: String,
            totalAmount: Int,
            emailId: String,
            firstName: String,
            mobileNumber: String,
            productId: String,
            paymentMode: String = "All",
            subPaymentMode: String? = null
        ): Response<CreateOrderResponse>? {
            return getInstance()?.createOrder(
                shopBaseUrl,
                totalAmount,
                emailId,
                firstName,
                mobileNumber,
                productId,
                paymentMode,
                subPaymentMode
            )
        }

        /**
         * Check if SDK is properly initialized
         * @return true if SDK is initialized, false otherwise
         */
        fun isInitialized(): Boolean {
            return nimbblApiRepository != null && BASE_URL.isNotEmpty() && FINGERPRINT.isNotEmpty() && DEVICE_FINGERPRINT.isNotEmpty()
        }
        
        /**
         * Reset the repository instance (useful for testing or re-initialization)
         */
        fun resetRepository() {
            nimbblApiRepository = null
            if (is_debug_enabled) {
                Log.d("NimbblCoreApiSDK", "Repository reset")
            }
        }
        
        /**
         * Get API repository instance with null safety
         */
        fun getAPIRepositoryInstance(): NimbblRepository? {
            if (nimbblApiRepository == null) {
                try {
                    // Initialize the repository with proper web service
                    val webService = CoreAppWebService(
                        BASE_URL, 
                        DEVICE_FINGERPRINT, 
                        FINGERPRINT, 
                        getIPAddress(true)
                    )
                    
                    if (webService != null) {
                        nimbblApiRepository = NimbblRepositoryImpl(webService)
                        if (is_debug_enabled) {
                            Log.d("NimbblCoreApiSDK", "Repository initialized successfully")
                        }
                    } else {
                        Log.e("NimbblCoreApiSDK", "Failed to create web service")
                    }
                } catch (e: Exception) {
                    Log.e("NimbblCoreApiSDK", "Error initializing repository: ${e.message}", e)
                }
            }
            
            return nimbblApiRepository?.also {
                if (it == null) {
                    Log.e("NimbblCoreApiSDK", "Repository instance is null")
                }
            } ?: run {
                Log.e("NimbblCoreApiSDK", "Repository instance is null, cannot proceed")
                null
            }
        }
        
        fun getOrderCreationServiceInstance(): OrderCreationService? {
            if (orderCreationService == null) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.nimbbl.tech/") // Use a real base URL that will be overridden
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                
                orderCreationService = retrofit.create(OrderCreationService::class.java)
            }
            return orderCreationService
        }
    }
}
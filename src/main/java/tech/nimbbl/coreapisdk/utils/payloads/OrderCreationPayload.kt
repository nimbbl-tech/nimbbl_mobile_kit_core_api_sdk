package tech.nimbbl.coreapisdk.utils.payloads

import com.google.gson.Gson
import com.google.gson.JsonObject
import tech.nimbbl.coreapisdk.api.models.requests.CreateOrderRequest
import tech.nimbbl.coreapisdk.api.models.requests.UserRequest

/**
 * Utility class for creating order creation payloads
 * Matches iOS implementation structure
 */
object OrderCreationPayload {
    
    /**
     * Create order payload matching iOS implementation
     * @param totalAmount The total amount for the order
     * @param emailId Customer email ID
     * @param firstName Customer first name
     * @param mobileNumber Customer mobile number
     * @param productId Product ID
     * @param paymentMode Payment mode (optional, defaults to "All")
     * @param subPaymentMode Sub payment mode (optional)
     * @return JSON string payload for order creation
     */
    fun createOrderPayload(
        totalAmount: Int,
        emailId: String,
        firstName: String,
        mobileNumber: String,
        productId: String,
        paymentMode: String = "All",
        subPaymentMode: String? = null
    ): String {
        val payload = JsonObject().apply {
            addProperty("currency", "INR")
            addProperty("amount", totalAmount.toString())
            addProperty("product_id", productId)
            addProperty("orderLineItems", true)
            addProperty("checkout_experience", "redirect")
            addProperty("payment_mode", paymentMode.ifEmpty { "All" })
            
            // Add subPaymentMode if provided
            if (!subPaymentMode.isNullOrEmpty()) {
                addProperty("subPaymentMode", subPaymentMode)
            }
            
            // Add user information if mobile number is provided (matching iOS structure)
            if (mobileNumber.isNotEmpty()) {
                val userJson = JsonObject().apply {
                    addProperty("email", emailId)
                    addProperty("name", firstName)
                    addProperty("mobile_number", mobileNumber)
                }
                add("user", userJson)
            }
        }
        
        return Gson().toJson(payload)
    }
    
    /**
     * Create shop order request object (iOS-compatible version)
     * @param currency Currency code (default: "INR")
     * @param amount Amount as string
     * @param productId Product ID
     * @param orderLineItems Whether to include order line items
     * @param checkoutExperience Checkout experience type
     * @param paymentMode Payment mode
     * @param subPaymentMode Sub payment mode
     * @param userEmail User email
     * @param userName User name
     * @param userMobileNumber User mobile number
     * @return CreateOrderRequest object for shop order creation
     */
    fun createShopOrderRequest(
        currency: String = "INR",
        amount: String,
        productId: String,
        orderLineItems: Boolean = true,
        checkoutExperience: String = "redirect",
        paymentMode: String = "All",
        subPaymentMode: String? = null,
        userEmail: String = "",
        userName: String = "",
        userMobileNumber: String = ""
    ): CreateOrderRequest {
        val user = if (userEmail.isNotEmpty() || userName.isNotEmpty() || userMobileNumber.isNotEmpty()) {
            UserRequest(
                email = userEmail,
                name = userName,
                mobileNumber = userMobileNumber
            )
        } else {
            null
        }
        
        return CreateOrderRequest(
            currency = currency,
            amount = amount,
            productId = productId,
            orderLineItems = orderLineItems,
            checkoutExperience = checkoutExperience,
            paymentMode = paymentMode,
            subPaymentMode = subPaymentMode,
            user = user
        )
    }
    
    /**
     * Create shop order payload (iOS-compatible version) - String version for backward compatibility
     * @param currency Currency code (default: "INR")
     * @param amount Amount as string
     * @param productId Product ID
     * @param orderLineItems Whether to include order line items
     * @param checkoutExperience Checkout experience type
     * @param paymentMode Payment mode
     * @param subPaymentMode Sub payment mode
     * @param userEmail User email
     * @param userName User name
     * @param userMobileNumber User mobile number
     * @return JSON string payload for shop order creation
     */
    fun createShopOrderPayload(
        currency: String = "INR",
        amount: String,
        productId: String,
        orderLineItems: Boolean = true,
        checkoutExperience: String = "redirect",
        paymentMode: String = "All",
        subPaymentMode: String? = null,
        userEmail: String = "",
        userName: String = "",
        userMobileNumber: String = ""
    ): String {
        val payload = JsonObject().apply {
            addProperty("currency", currency)
            addProperty("amount", amount)
            addProperty("product_id", productId)
            addProperty("orderLineItems", orderLineItems)
            addProperty("checkout_experience", checkoutExperience)
            addProperty("payment_mode", paymentMode)
            
            // Add subPaymentMode if provided
            if (!subPaymentMode.isNullOrEmpty()) {
                addProperty("subPaymentMode", subPaymentMode)
            }
            
            // Add user information (matching iOS structure)
            if (userEmail.isNotEmpty() || userName.isNotEmpty() || userMobileNumber.isNotEmpty()) {
                val userJson = JsonObject().apply {
                    addProperty("email", userEmail)
                    addProperty("name", userName)
                    addProperty("mobile_number", userMobileNumber)
                }
                add("user", userJson)
            }
        }
        
        return Gson().toJson(payload)
    }
} 
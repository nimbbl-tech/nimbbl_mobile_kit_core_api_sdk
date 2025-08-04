package tech.nimbbl.coreapisdk.api.models.requests

import com.google.gson.annotations.SerializedName

/**
 * Request model for creating shop orders
 * Matches iOS implementation structure
 */
data class CreateOrderRequest(
    @SerializedName("currency")
    val currency: String,
    
    @SerializedName("amount")
    val amount: String,
    
    @SerializedName("product_id")
    val productId: String,
    
    @SerializedName("orderLineItems")
    val orderLineItems: Boolean,
    
    @SerializedName("checkout_experience")
    val checkoutExperience: String,
    
    @SerializedName("payment_mode")
    val paymentMode: String,
    
    @SerializedName("subPaymentMode")
    val subPaymentMode: String? = null,
    
    @SerializedName("user")
    val user: UserRequest? = null
)

/**
 * User information for order creation
 */
data class UserRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("mobile_number")
    val mobileNumber: String
) 
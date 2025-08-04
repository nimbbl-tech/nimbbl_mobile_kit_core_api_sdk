package tech.nimbbl.coreapisdk.api.models.responses

import com.google.gson.annotations.SerializedName
import tech.nimbbl.coreapisdk.data.models.user.User

data class CreateOrderResponse(
    @SerializedName("amount_before_tax")
    val amountBeforeTax: Int,
    @SerializedName("attempts")
    val attempts: Int,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("currency_conversion")
    val currencyConversion: CurrencyConversion?,
    @SerializedName("invoice_id")
    val invoiceId: String,
    @SerializedName("next")
    val next: List<Next>?,
    @SerializedName("order_date")
    val orderDate: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("refresh_token_expiration")
    val refreshTokenExpiration: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("tax")
    val tax: Int,
    @SerializedName("token")
    val token: String,
    @SerializedName("token_expiration")
    val tokenExpiration: String,
    @SerializedName("total_amount")
    val totalAmount: Int,
    @SerializedName("user")
    val user: User?
)

data class CurrencyConversion(
    @SerializedName("conversion_rate")
    val conversionRate: Double?,
    @SerializedName("from_currency")
    val fromCurrency: String?,
    @SerializedName("to_currency")
    val toCurrency: String?
)

data class Next(
    @SerializedName("action")
    val action: String?,
    @SerializedName("url")
    val url: String?
) 
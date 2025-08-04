package tech.nimbbl.coreapisdk.api.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import tech.nimbbl.coreapisdk.api.models.requests.CreateOrderRequest
import tech.nimbbl.coreapisdk.api.models.responses.CreateOrderResponse

/**
 * Service for order creation functionality
 */
interface OrderCreationService {
    
    /**
     * Create a new order
     * @param url The order creation endpoint URL
     * @param orderPayload The order creation payload
     * @return Response containing the created order details
     */
    @POST
    suspend fun createOrder(
        @Url url: String,
        @Body orderPayload: CreateOrderRequest
    ): Response<CreateOrderResponse>
} 
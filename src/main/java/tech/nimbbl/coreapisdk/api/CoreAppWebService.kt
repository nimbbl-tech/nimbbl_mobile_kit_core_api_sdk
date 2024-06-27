package tech.nimbbl.coreapisdk.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url
import tech.nimbbl.coreapisdk.api.ServiceConstants.Companion.BASE_URL
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
import tech.nimbbl.coreapisdk.models.transaction_enquiry.TransactionEnquiryResponseVo
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_fingerPrint
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_ipAddress
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_userAgent
import tech.nimbbl.coreapisdk.utils.printLog
import java.util.concurrent.TimeUnit


interface CoreAppWebService {

    @POST
    suspend fun cancelCheckout(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body body: RequestBody
    ): Response<Void>


    @PATCH
    suspend fun updateOrder(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body body: RequestBody
    ): Response<OrderResponse>

    @GET
    suspend fun checkOutResource(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
    ): Response<CheckoutResourceVo>


    @GET
    suspend fun downloadBankLogo(@Url fileUrl: String): Response<ResponseBody>

    @GET
    suspend fun getOrderDetails(
        @Url url: String,
        @Header("Authorization") auth: String
    ): Response<OrderResponse>

    @POST
    suspend fun resolveUser(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<ResolveUserResponse>

    @POST
    suspend fun verifyUser(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<ResolveUserResponse>

    @POST
    suspend fun getPaymentModes(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Header("x-nimbbl-user-token") nimbblUserToken: String,
        @Body inputPayload: RequestBody
    ): Response<PaymentModesResponse>

    @POST
    suspend fun getListOfBanks(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<ListOfBankResponse>

    @POST
    suspend fun getListOfWallets(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<ListOfWalletResponse>

    @POST
    suspend fun initiatePayment(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Header("x-nimbbl-user-token") nimbblUserToken: String,
        @Body inputPayload: RequestBody
    ): Response<InitiatePaymentResponse>

    @POST
    suspend fun makePayment(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Header("x-nimbbl-user-token") nimbblUserToken: String,
        @Body inputPayload: RequestBody
    ): Response<InitiatePaymentResponse>

    @POST
    suspend fun getTransactionEnquiry(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<TransactionEnquiryResponseVo>

    @POST
    suspend fun getBinData(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<BinDataResponse>

    @GET
    suspend fun getPublicKey(
        @Url url: String
    ): Response<PublicKeyResponse>

    @POST
    suspend fun resendOtp(
        @Url url: String,
        @Header("x-nimbbl-key") nimbblKey: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<ResendOtpResponse>

    @PUT
    suspend fun updateTransaction(
        @Url url: String,
        @Header("Authorization") auth: String,
        @Body inputPayload: RequestBody
    ): Response<UpdateTransactionResponse>


    companion object {
        // private const val BASE_URL = "https://uatshop.nimbbl.tech/api/"
        private var retrofit: Retrofit? = null

        operator fun invoke(
            baseUrl: String,
            userAgent: String,
            md5: String,
            ipAddress: String
        ): CoreAppWebService? {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(Interceptor { chain ->
                    val request: Request =
                        chain.request().newBuilder()
                            .addHeader(key_userAgent, userAgent)
                            .addHeader(key_ipAddress, ipAddress)
                            .addHeader(key_fingerPrint, md5)
                            .build()
                    chain.proceed(request)
                })
                .build()
            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            printLog("SAN", BASE_URL)

            return retrofit!!.create(CoreAppWebService::class.java)
        }
    }
}

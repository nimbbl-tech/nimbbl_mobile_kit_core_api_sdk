package tech.nimbbl.coreapisdk.interfaces

interface INimbblCheckoutBaseSDKInterface {

    fun initSDK(app_access_key: String,authToken: String,packageName:String)

    fun updateOrder(orderId:String,authToken: String)

    fun resolveUser()

    fun fetchPaymentModeDetails(orderId: String)

    fun initiatePayment(payload: String)

    //internal method for opening upi app
    fun openUPIApp(queryString: String,packageName:String)

    //opening bank url  in webview
    fun openingBankPage(url:String)

    fun getbinData() //for cc dc payment

    fun getTransactionEnquiry()
}
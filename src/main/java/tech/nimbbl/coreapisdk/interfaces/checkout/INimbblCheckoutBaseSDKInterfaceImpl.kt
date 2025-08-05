package tech.nimbbl.coreapisdk.interfaces.checkout



class INimbblCheckoutBaseSDKInterfaceImpl private constructor(): INimbblCheckoutBaseSDKInterface {

     override fun openUPIApp(queryString: String, packageName: String) {
         TODO("Not yet implemented")
     }

     override fun openingBankPage(url: String) {
         TODO("Not yet implemented")
     }

     override fun getbinData() {
         TODO("Not yet implemented")
     }

     override fun getTransactionEnquiry() {
         TODO("Not yet implemented")
     }


     override fun initSDK(app_access_key: String, authToken: String, packageName: String) {
         TODO("Not yet implemented")
     }

     override fun updateOrder(orderId: String, authToken: String) {
         TODO("Not yet implemented")
     }

     override fun resolveUser() {
         TODO("Not yet implemented")
     }

     override fun fetchPaymentModeDetails(orderId: String) {
         TODO("Not yet implemented")
     }

     override fun initiatePayment(payload: String) {
         TODO("Not yet implemented")
     }


 }
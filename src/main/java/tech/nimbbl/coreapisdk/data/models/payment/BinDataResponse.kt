package tech.nimbbl.coreapisdk.data.models.payment

import tech.nimbbl.coreapisdk.data.models.common.Error as NimbblError

data class BinDataResponse(
    val `data`: BinData?,
    val error: NimbblError?,
    val status_code: Int?


)
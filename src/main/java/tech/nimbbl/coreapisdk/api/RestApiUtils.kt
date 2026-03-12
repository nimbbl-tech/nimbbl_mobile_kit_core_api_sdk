package tech.nimbbl.coreapisdk.api

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/
object RestApiUtils {

    // Default Production URLs
    var NIMBBL_TECH_URL = "https://api.nimbbl.tech/"
    var WEB_VIEW_VIEW_URL = "https://sonic.nimbbl.tech/?token=%1\$s"
    var WEB_VIEW_RESP_CHECK_URL = "https://sonic.nimbbl.tech/mobile/redirect"

    // Event Logging URLs
    private const val EVENT_LOG_URL_PP = "https://eventlogpipepp.nimbbl.tech/v1/log"
    private const val EVENT_LOG_URL_PROD = "https://eventlogpipe.nimbbl.tech/v1/log"

    // Tenant IDs
    private const val TENANT_ID_QA = "5fd6a596-a39f-4cb2-9a4b-ed72713e537e"
    private const val TENANT_ID_PP = "5fd6a596-a39f-4cb2-9a4b-ed72713e537e"
    private const val TENANT_ID_PROD = "6e7e900c-9ce6-4271-b1ec-08875a9129d1"

    private fun isIpBasedUrl(url: String): Boolean =
        url.matches(Regex("https?://\\d+\\.\\d+\\.\\d+\\.\\d+(:\\d+)?/?.*"))

    private fun qaEventLogUrl(qaNumber: String) =
        "https://qa${qaNumber}eventlogpipe.qa.nimbbl.tech/v1/log"

    /**
     * Get event log URL based on NIMBBL_TECH_URL environment.
     * IP-based URLs (e.g. http://192.168.1.100:8080/) -> qa1.
     * QA format: https://qa{N}eventlogpipe.qa.nimbbl.tech/v1/log (e.g. qa1api.qa.nimbbl.tech -> qa1eventlogpipe.qa.nimbbl.tech)
     */
    fun getEventLogUrl(): String {
        val url = NIMBBL_TECH_URL
        return when {
            isIpBasedUrl(url) -> qaEventLogUrl("1")
            url.contains("qa") -> {
                val qaNumber = Regex("qa(\\d+)").find(url)?.groupValues?.get(1) ?: "1"
                qaEventLogUrl(qaNumber)
            }
            url.contains("pp") -> EVENT_LOG_URL_PP
            else -> EVENT_LOG_URL_PROD
        }
    }

    /**
     * Get default tenant ID based on NIMBBL_TECH_URL environment.
     * IP-based URLs -> qa1 tenant.
     */
    fun getDefaultTenantId(): String {
        val url = NIMBBL_TECH_URL
        return when {
            isIpBasedUrl(url) -> TENANT_ID_QA
            url.contains("qa") -> TENANT_ID_QA
            url.contains("pp") -> TENANT_ID_PP
            else -> TENANT_ID_PROD
        }
    }

    /**
     * Get WebView URL for any environment
     * @param baseUrl Base URL (e.g., "https://qa1api.qa.nimbbl.tech/" or "http://192.168.1.100:8080/")
     * @return WebView URL with token placeholder
     */
    fun getWebViewUrl(baseUrl: String): String {
        // Check if input is an IP address (with optional port)
        val isIpAddress = baseUrl.matches(Regex("https?://\\d+\\.\\d+\\.\\d+\\.\\d+(:\\d+)?/?.*"))

        return if (isIpAddress) {
            // For IP addresses, use the same base URL for WebView
            val normalizedUrl = baseUrl.removeSuffix("/")
            "$normalizedUrl/?token=%1\$s"
        } else {
            // For domain names, transform to sonic domain
            val sonicDomain = baseUrl
                .replace("api.nimbbl.tech", "sonic.nimbbl.tech")
                .replace("apipp.nimbbl.tech", "sonicpp.nimbbl.tech")
                .replace(Regex("qa(\\d+)api\\.qa\\.nimbbl\\.tech"), "qa$1sonic.qa.nimbbl.tech")
                .removeSuffix("/")
            "$sonicDomain/?token=%1\$s"
        }
    }

    /**
     * Get WebView Response Check URL for any environment
     * @param baseUrl Base URL (e.g., "https://qa1api.qa.nimbbl.tech/" or "http://192.168.1.100:8080/")
     * @return WebView response redirect URL
     */
    fun getWebViewResponseUrl(baseUrl: String): String {
        // Check if input is an IP address (with optional port)
        val isIpAddress = baseUrl.matches(Regex("https?://\\d+\\.\\d+\\.\\d+\\.\\d+(:\\d+)?/?.*"))

        return if (isIpAddress) {
            // For IP addresses, use the same base URL with /mobile/redirect path
            val normalizedUrl = baseUrl.removeSuffix("/")
            "$normalizedUrl/mobile/redirect"
        } else {
            // For domain names, transform to sonic domain
            val sonicDomain = baseUrl
                .replace("api.nimbbl.tech", "sonic.nimbbl.tech")
                .replace("apipp.nimbbl.tech", "sonicpp.nimbbl.tech")
                .replace(Regex("qa(\\d+)api\\.qa\\.nimbbl\\.tech"), "qa$1sonic.qa.nimbbl.tech")
                .removeSuffix("/")
            "$sonicDomain/mobile/redirect"
        }
    }
}


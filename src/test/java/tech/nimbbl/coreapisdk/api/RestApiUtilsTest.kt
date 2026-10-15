package tech.nimbbl.coreapisdk.api

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class RestApiUtilsTest {

    private val originalUrl = RestApiUtils.NIMBBL_TECH_URL

    @After
    fun tearDown() {
        // NIMBBL_TECH_URL is mutable global state read by other SDK classes at runtime.
        RestApiUtils.NIMBBL_TECH_URL = originalUrl
    }

    @Test
    fun getEventLogUrl_production() {
        RestApiUtils.NIMBBL_TECH_URL = "https://api.nimbbl.tech/"
        assertEquals("https://eventlogpipe.nimbbl.tech/v1/log", RestApiUtils.getEventLogUrl())
    }

    @Test
    fun getEventLogUrl_preProd() {
        RestApiUtils.NIMBBL_TECH_URL = "https://apipp.nimbbl.tech/"
        assertEquals("https://eventlogpipepp.nimbbl.tech/v1/log", RestApiUtils.getEventLogUrl())
    }

    @Test
    fun getEventLogUrl_qaUsesMatchingNumber() {
        RestApiUtils.NIMBBL_TECH_URL = "https://qa3api.qa.nimbbl.tech/"
        assertEquals("https://qa3eventlogpipe.qa.nimbbl.tech/v1/log", RestApiUtils.getEventLogUrl())
    }

    @Test
    fun getEventLogUrl_qaWithoutNumberDefaultsToOne() {
        RestApiUtils.NIMBBL_TECH_URL = "https://qaapi.qa.nimbbl.tech/"
        assertEquals("https://qa1eventlogpipe.qa.nimbbl.tech/v1/log", RestApiUtils.getEventLogUrl())
    }

    @Test
    fun getEventLogUrl_ipBasedUrlDefaultsToQa1() {
        RestApiUtils.NIMBBL_TECH_URL = "http://192.168.1.100:8080/"
        assertEquals("https://qa1eventlogpipe.qa.nimbbl.tech/v1/log", RestApiUtils.getEventLogUrl())
    }

    @Test
    fun getDefaultTenantId_variesByEnvironment() {
        RestApiUtils.NIMBBL_TECH_URL = "https://api.nimbbl.tech/"
        val prodTenant = RestApiUtils.getDefaultTenantId()

        RestApiUtils.NIMBBL_TECH_URL = "https://qa1api.qa.nimbbl.tech/"
        val qaTenant = RestApiUtils.getDefaultTenantId()

        RestApiUtils.NIMBBL_TECH_URL = "http://192.168.1.100:8080/"
        val ipTenant = RestApiUtils.getDefaultTenantId()

        assertEquals(qaTenant, ipTenant)
        assert(prodTenant != qaTenant) { "prod and qa tenant ids must differ" }
    }

    @Test
    fun getWebViewUrl_prodDomainMapsToSonic() {
        assertEquals(
            "https://sonic.nimbbl.tech/?token=%1\$s",
            RestApiUtils.getWebViewUrl("https://api.nimbbl.tech/")
        )
    }

    @Test
    fun getWebViewUrl_qaDomainMapsToMatchingSonic() {
        assertEquals(
            "https://qa3sonic.qa.nimbbl.tech/?token=%1\$s",
            RestApiUtils.getWebViewUrl("https://qa3api.qa.nimbbl.tech/")
        )
    }

    @Test
    fun getWebViewUrl_ipAddressKeepsSameHost() {
        assertEquals(
            "http://192.168.1.100:8080/?token=%1\$s",
            RestApiUtils.getWebViewUrl("http://192.168.1.100:8080/")
        )
    }

    @Test
    fun getWebViewResponseUrl_prodDomainMapsToSonic() {
        assertEquals(
            "https://sonic.nimbbl.tech/mobile/redirect",
            RestApiUtils.getWebViewResponseUrl("https://api.nimbbl.tech/")
        )
    }

    @Test
    fun getWebViewResponseUrl_ipAddressKeepsSameHost() {
        assertEquals(
            "http://192.168.1.100:8080/mobile/redirect",
            RestApiUtils.getWebViewResponseUrl("http://192.168.1.100:8080/")
        )
    }
}

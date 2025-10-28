package tech.nimbbl.coreapisdk

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import tech.nimbbl.coreapisdk.core.constants.Constants
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants

/**
 * Test class for Constants dynamic configuration based on BASE_URL
 */
class ConstantsTest {
    
    @Before
    fun setUp() {
        // Reset BASE_URL to default before each test
        ServiceConstants.BASE_URL = "https://api.nimbbl.tech/"
    }
    
    @Test
    fun testEventLogUrlForProduction() {
        ServiceConstants.BASE_URL = "https://api.nimbbl.tech/"
        assertEquals("https://eventlogpipe.nimbbl.tech/v1/log", Constants.EVENT_LOG_URL)
    }
    
    @Test
    fun testEventLogUrlForQA() {
        ServiceConstants.BASE_URL = "https://qaapi.nimbbl.tech/"
        assertEquals("https://eventlogpipepp.nimbbl.tech/v1/log", Constants.EVENT_LOG_URL)
    }
    
    @Test
    fun testEventLogUrlForPP() {
        ServiceConstants.BASE_URL = "https://ppapi.nimbbl.tech/"
        assertEquals("https://eventlogpipepp.nimbbl.tech/v1/log", Constants.EVENT_LOG_URL)
    }
    
    @Test
    fun testEventLogUrlForFallback() {
        ServiceConstants.BASE_URL = "https://unknown.nimbbl.tech/"
        assertEquals("https://eventlogpipe.nimbbl.tech/v1/log", Constants.EVENT_LOG_URL)
    }
    
    @Test
    fun testDefaultTenantIdForProduction() {
        ServiceConstants.BASE_URL = "https://api.nimbbl.tech/"
        assertEquals("6e7e900c-9ce6-4271-b1ec-08875a9129d1", Constants.DEFAULT_TENANT_ID)
    }
    
    @Test
    fun testDefaultTenantIdForQA() {
        ServiceConstants.BASE_URL = "https://qaapi.nimbbl.tech/"
        assertEquals("5fd6a596-a39f-4cb2-9a4b-ed72713e537e", Constants.DEFAULT_TENANT_ID)
    }
    
    @Test
    fun testDefaultTenantIdForPP() {
        ServiceConstants.BASE_URL = "https://ppapi.nimbbl.tech/"
        assertEquals("5fd6a596-a39f-4cb2-9a4b-ed72713e537e", Constants.DEFAULT_TENANT_ID)
    }
    
    @Test
    fun testDefaultTenantIdForFallback() {
        ServiceConstants.BASE_URL = "https://unknown.nimbbl.tech/"
        assertEquals("6e7e900c-9ce6-4271-b1ec-08875a9129d1", Constants.DEFAULT_TENANT_ID)
    }
} 
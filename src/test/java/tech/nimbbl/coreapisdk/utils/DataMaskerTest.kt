package tech.nimbbl.coreapisdk.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMaskerTest {

    @Test
    fun `sanitizeForLogging masks email, otp, phone, name and address fields`() {
        val body = """{"email":"john.doe@example.com","first_name":"John","last_name":"Doe",""" +
            """"mobile_number":"9876543210","country_code":"+91","otp":"123456",""" +
            """"address_1":"1080 Beach Mansion","token":"eyJabcdefghijklmnop.body.sig"}"""

        val sanitized = DataMasker.sanitizeForLogging(body)!!

        assertFalse("raw email leaked", sanitized.contains("john.doe@example.com"))
        assertTrue("masked email missing", sanitized.contains("\"j***@example.com\""))

        assertFalse("raw otp leaked", sanitized.contains("123456"))
        assertTrue("masked otp missing", sanitized.contains("\"otp\":\"***\""))

        assertFalse("raw phone leaked", sanitized.contains("9876543210"))
        assertTrue("masked phone missing", sanitized.contains("\"********10\""))

        assertFalse("raw first_name leaked", sanitized.contains("\"John\""))
        assertFalse("raw last_name leaked", sanitized.contains("\"Doe\""))
        assertFalse("raw address leaked", sanitized.contains("1080 Beach Mansion"))

        assertFalse("raw token leaked", sanitized.contains("eyJabcdefghijklmnop.body.sig"))
    }

    @Test
    fun `sanitizeForLogging leaves empty PII values untouched`() {
        val body = """{"email":"","mobile_number":""}"""
        assertEquals(body, DataMasker.sanitizeForLogging(body))
    }

    @Test
    fun `sanitizeForLogging handles null and blank input`() {
        assertEquals(null, DataMasker.sanitizeForLogging(null))
        assertEquals("", DataMasker.sanitizeForLogging(""))
    }
}

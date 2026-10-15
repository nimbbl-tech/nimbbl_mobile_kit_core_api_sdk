package tech.nimbbl.coreapisdk.utils.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * md5()/getDeviceID()/getSessionID() only touch android.os.Build's static fields (which the
 * Android stub jar returns default values for, not methods, so no "not mocked" crash) — no
 * device/emulator needed for these specific functions.
 */
class NimbblSDKExtensionsTest {

    @Test
    fun md5_isDeterministicAndHex32() {
        val hash1 = md5("hello")
        val hash2 = md5("hello")
        assertEquals(hash1, hash2)
        assertEquals(32, hash1.length)
        assertTrue(hash1.matches(Regex("[0-9a-f]{32}")))
    }

    @Test
    fun md5_differentInputsProduceDifferentHashes() {
        assertTrue(md5("a") != md5("b"))
    }

    @Test
    fun getDeviceID_neverThrowsAndReturnsNonEmptyString() {
        assertTrue(getDeviceID().isNotEmpty())
    }

    @Test
    fun getSessionID_neverThrowsAndReturnsNonEmptyString() {
        assertTrue(getSessionID().isNotEmpty())
    }
}

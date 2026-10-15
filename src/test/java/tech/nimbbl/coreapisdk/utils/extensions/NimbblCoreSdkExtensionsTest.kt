package tech.nimbbl.coreapisdk.utils.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Covers the pure-JVM functions in NimbblCoreSdkExtensions.kt (no org.json/android.* calls
 * on these specific code paths), so no device/emulator is needed.
 */
class NimbblCoreSdkExtensionsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun getXNimbblKey_isDeterministicAndPrefixedWithSubMerchantId() {
        val key1 = getXNimbblKey("sub_1", "payload")
        val key2 = getXNimbblKey("sub_1", "payload")
        assertEquals(key1, key2)
        assertTrue(key1.startsWith("sub_1-"))
        // "sub_1-" + 32 hex chars of MD5
        assertEquals(6 + 32, key1.length)
    }

    @Test
    fun getXNimbblKey_differentInputsProduceDifferentHashes() {
        val key1 = getXNimbblKey("sub_1", "payload_a")
        val key2 = getXNimbblKey("sub_1", "payload_b")
        assertTrue(key1 != key2)
    }

    @Test
    fun createDirIfNotExists_createsMissingDirectory() {
        val target = File(tempFolder.root, "nested/dir")
        assertFalse(target.exists())
        assertTrue(createDirIfNotExists(target.absolutePath))
        assertTrue(target.exists() && target.isDirectory)
    }

    @Test
    fun createDirIfNotExists_returnsTrueWhenAlreadyADirectory() {
        assertTrue(createDirIfNotExists(tempFolder.root.absolutePath))
    }

    @Test
    fun writeResponseBodyToDisk_writesBytesAndReturnsPath() {
        val path = writeResponseBodyToDisk(tempFolder.root.absolutePath, "hello".toByteArray(), "out.txt")
        assertTrue(path.isNotEmpty())
        val written = File(path)
        assertTrue(written.exists())
        assertEquals("hello", written.readText())
    }

    @Test
    fun writeResponseBodyToDisk_nullBodyReturnsEmptyString() {
        assertEquals("", writeResponseBodyToDisk(tempFolder.root.absolutePath, null, "out.txt"))
    }

    @Test
    fun getIPAddress_doesNotCrashAndReturnsAString() {
        // Uses plain java.net (not android.net), so this runs fine on the JVM.
        // Result depends on the host's network interfaces; just assert it completes safely.
        getIPAddress(useIPv4 = true)
        getIPAddress(useIPv4 = false)
    }

    @Test
    fun printLog_doesNotThrow() {
        printLog("TestTag", "test message")
    }
}

package tech.nimbbl.coreapisdk.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoggingConfigTest {

    @Test
    fun setLogLevel_changesShouldLogThreshold() {
        LoggingConfig.setLoggingEnabled(true)
        LoggingConfig.setLogLevel(LoggingConfig.LogLevel.ERROR)

        assertFalse(LoggingConfig.shouldLog(LoggingConfig.LogLevel.DEBUG))
        assertTrue(LoggingConfig.shouldLog(LoggingConfig.LogLevel.ERROR))
    }

    @Test
    fun setLoggingEnabled_false_suppressesEverything() {
        LoggingConfig.setLogLevel(LoggingConfig.LogLevel.VERBOSE)
        LoggingConfig.setLoggingEnabled(false)

        assertFalse(LoggingConfig.shouldLog(LoggingConfig.LogLevel.ERROR))

        LoggingConfig.setLoggingEnabled(true) // don't leak state into other tests
    }

    @Test
    fun getCurrentConfiguration_reflectsLastSetValues() {
        LoggingConfig.setLogLevel(LoggingConfig.LogLevel.WARNING)
        LoggingConfig.setLoggingEnabled(true)

        val (level, enabled) = LoggingConfig.getCurrentConfiguration()
        assertEquals(LoggingConfig.LogLevel.WARNING, level)
        assertTrue(enabled)
    }

    @Test
    fun configureForDevelopmentAndProduction_doNotCrash() {
        LoggingConfig.configureForDevelopment()
        LoggingConfig.configureForProduction()
    }

    @Test
    fun convenienceLogMethods_doNotCrashAtAnyLevel() {
        LoggingConfig.setLoggingEnabled(true)
        LoggingConfig.setLogLevel(LoggingConfig.LogLevel.VERBOSE)
        LoggingConfig.verbose("TAG", "v")
        LoggingConfig.debug("TAG", "d")
        LoggingConfig.info("TAG", "i")
        LoggingConfig.warning("TAG", "w")
        LoggingConfig.error("TAG", "e", IllegalStateException("boom"))
    }

    @Test
    fun log_belowThreshold_isSuppressedWithoutCrashing() {
        LoggingConfig.setLoggingEnabled(true)
        LoggingConfig.setLogLevel(LoggingConfig.LogLevel.ERROR)
        LoggingConfig.log(LoggingConfig.LogLevel.DEBUG, "TAG", "should be suppressed")
    }
}

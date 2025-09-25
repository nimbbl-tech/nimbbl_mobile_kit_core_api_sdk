package tech.nimbbl.coreapisdk.utils

import android.util.Log
import tech.nimbbl.coreapisdk.core.constants.Constants.is_debug_enabled

/**
 * Logging configuration utility for the Nimbbl Android SDK
 * Provides centralized control over logging behavior for production vs development
 */
object LoggingConfig {
    
    private const val TAG = "LoggingConfig"
    
    // Log levels
    enum class LogLevel(val priority: Int) {
        VERBOSE(0),
        DEBUG(1),
        INFO(2),
        WARNING(3),
        ERROR(4)
    }
    
    // Current configuration
    private var currentLogLevel: LogLevel = if (is_debug_enabled) LogLevel.DEBUG else LogLevel.WARNING
    private var isLoggingEnabled: Boolean = true
    
    /**
     * Configure logging for production builds
     * Sets log level to ERROR only and reduces verbosity
     */
    fun configureForProduction() {
        if (!is_debug_enabled) {
            currentLogLevel = LogLevel.ERROR
            isLoggingEnabled = true
            Log.i(TAG, "Logging configured for production - ERROR level only")
        }
    }
    
    /**
     * Configure logging for development builds
     * Sets log level to DEBUG for maximum verbosity
     */
    fun configureForDevelopment() {
        if (is_debug_enabled) {
            currentLogLevel = LogLevel.DEBUG
            isLoggingEnabled = true
            Log.d(TAG, "Logging configured for development - DEBUG level")
        }
    }
    
    /**
     * Set custom log level
     */
    fun setLogLevel(level: LogLevel) {
        currentLogLevel = level
        Log.i(TAG, "Log level set to: $level")
    }
    
    /**
     * Enable or disable logging
     */
    fun setLoggingEnabled(enabled: Boolean) {
        isLoggingEnabled = enabled
        Log.i(TAG, "Logging ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Check if a log level should be output
     */
    fun shouldLog(level: LogLevel): Boolean {
        return isLoggingEnabled && level.priority >= currentLogLevel.priority
    }
    
    /**
     * Get current configuration
     */
    fun getCurrentConfiguration(): Pair<LogLevel, Boolean> {
        return Pair(currentLogLevel, isLoggingEnabled)
    }
    
    /**
     * Log a message with level checking
     */
    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(level)) return
        
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARNING -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
    }
    
    /**
     * Convenience methods for different log levels
     */
    fun verbose(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.VERBOSE, tag, message, throwable)
    }
    
    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, message, throwable)
    }
    
    fun info(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, message, throwable)
    }
    
    fun warning(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARNING, tag, message, throwable)
    }
    
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
}

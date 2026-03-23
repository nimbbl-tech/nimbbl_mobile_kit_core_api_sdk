package tech.nimbbl.coreapisdk.core.constants

/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/

import android.util.Log
import tech.nimbbl.coreapisdk.BuildConfig
import tech.nimbbl.coreapisdk.api.RestApiUtils

object Constants {
    // Debug flag - automatically false in release builds
    val is_debug_enabled: Boolean
        get() = try {
            BuildConfig.DEBUG
        } catch (e: Exception) {
            Log.w("Constants", "Error accessing BuildConfig.DEBUG: ${e.message}", e)
            false // Default to false for safety
        }

    val sdk_version: String
        get() = BuildConfig.SDK_VERSION
    
    // Event Logging Configuration - Dynamic based on NIMBBL_TECH_URL
    val EVENT_LOG_URL: String
        get() = try {
            RestApiUtils.getEventLogUrl()
        } catch (e: Exception) {
            Log.w("Constants", "Error getting event log URL: ${e.message}", e)
            "" // Return empty string as fallback
        }
    
    val DEFAULT_TENANT_ID: String
        get() = try {
            RestApiUtils.getDefaultTenantId()
        } catch (e: Exception) {
            Log.w("Constants", "Error getting default tenant ID: ${e.message}", e)
            "" // Return empty string as fallback
        }
}
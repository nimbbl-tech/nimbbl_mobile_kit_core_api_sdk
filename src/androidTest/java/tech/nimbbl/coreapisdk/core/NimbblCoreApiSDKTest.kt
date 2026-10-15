package tech.nimbbl.coreapisdk.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import tech.nimbbl.coreapisdk.core.constants.ServiceConstants

@RunWith(AndroidJUnit4::class)
class NimbblCoreApiSDKTest {

    @Test
    fun getInstance_returnsSameSingleton() {
        assertSame(NimbblCoreApiSDK.getInstance(), NimbblCoreApiSDK.getInstance())
    }

    @Test
    fun initialiseAPISDK_setsEnvironmentFields() {
        NimbblCoreApiSDK.getInstance().initialiseAPISDK(
            url = "https://qa1api.qa.nimbbl.tech/",
            fingerPrint = "fp_123",
            deviceFingerPrint = "dfp_456",
            appCode = "app_1"
        )

        assertEquals("https://qa1api.qa.nimbbl.tech/", ServiceConstants.BASE_URL)
        assertEquals("fp_123", ServiceConstants.FINGERPRINT)
        assertEquals("dfp_456", ServiceConstants.DEVICE_FINGERPRINT)
    }

    @Test
    fun logEvent_withRealContext_doesNotCrash() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NimbblCoreApiSDK.getInstance().logEvent(context, "test_event")
    }
}

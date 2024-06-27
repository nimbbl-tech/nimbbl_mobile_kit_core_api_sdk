/*
 * Copyright @ 2017-present Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.nimbbl.coreapisdk.interfaces

/**
 * Interface for listening to events coming from Nimbbl Checkout SDK.
 */
interface NimbblCheckoutPaymentListener {
    /**
     * Called when a conference was joined.
     *
     * @param data Map with a "url" key with the conference URL.
     */
    fun onPaymentSuccess(data: MutableMap<String, Any>)

    /**
     * Called when the active conference ends, be it because of user choice or
     * because of a failure.
     *
     * @param data Map with an "error" key with the error and a "url" key with
     * the conference URL. If the conference finished gracefully no `error`
     * key will be present. The possible values for "error" are described here:
     * https://github.com/jitsi/lib-jitsi-meet/blob/master/JitsiConnectionErrors.js
     * https://github.com/jitsi/lib-jitsi-meet/blob/master/JitsiConferenceErrors.js
     */
    fun onPaymentFailed(data: String)
}
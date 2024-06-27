package tech.nimbbl.coreapisdk.utils

import org.json.JSONArray
import org.json.JSONObject
import tech.nimbbl.coreapisdk.models.BinData
import tech.nimbbl.coreapisdk.models.ResolveUserResponse
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_completePayment
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_getBinData
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_initiateOrder
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_initiatePayment
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_paymentEnquiry
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_paymentModes
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_resendOtp
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_resolveUser
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.action_validateCard
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.event_display_loader
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.event_exception_occured
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.event_hide_loader
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.event_process_result
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_OrderID
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_action
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_errorCode
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_errorMessage
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_event
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_issuer_name
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_message
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_mobileNumber
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_next
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_next_step
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_nimbblPayload
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_otp_sent
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_paymentModes
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_payment_mode
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_scheme_name
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_signature
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_status
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_sub_payment_code
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_sub_payment_name
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_transaction_id
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_user_name
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_vpa_account_holder
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_vpa_id
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.key_vpa_valid
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.status_success
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_payment_initiated
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_payment_mode_credit_card
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_payment_mode_debit_card
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_payment_mode_prepaid_card
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_sub_payment_code_credit
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_sub_payment_code_debit
import tech.nimbbl.coreapisdk.utils.PayloadKeys.Companion.value_sub_payment_code_prepaid


/*
Created by Sandeep Yadav on 23/02/22.
Copyright (c) 2022 Bigital Technologies Pvt. Ltd. All rights reserved.
*/



object ProcessOutputPayloads {

    fun getLoadingShowPayload(): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_display_loader)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }


    fun getLoadingHidePayload(): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_hide_loader)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }


    fun getExceptionOccuredPayload(
        action: String,
        errorCode: String,
        errorMessage: String
    ): JSONObject {
        val outputPayload = JSONObject()
        var tempErrorMeesage = errorMessage
        try {
            try {
                val jObjError = JSONObject(errorMessage)
                tempErrorMeesage = jObjError.getJSONObject("error").getString("message")
            } catch (e: java.lang.Exception) {
            }
            outputPayload.put(key_event, event_exception_occured)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action)
            nimbblOutputPayload.put(key_errorCode, errorCode)
            nimbblOutputPayload.put(key_errorMessage, tempErrorMeesage)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getInitiateOrderOutputPayload(): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_initiateOrder)
            nimbblOutputPayload.put(key_status, status_success)
            nimbblOutputPayload.put(key_next, action_paymentModes)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getPaymentModePayload(paymentModeJson: String?): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_paymentModes)
            nimbblOutputPayload.put(key_status, status_success)
            nimbblOutputPayload.put(key_paymentModes, JSONArray(paymentModeJson))
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getResolveUserOutputPayload(body: ResolveUserResponse?): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_resolveUser)
            nimbblOutputPayload.put(key_status, status_success)
            val firstName = body?.item?.first_name.toString()
            val lastName = if (body?.item?.last_name.toString().equals("null", true)) {
                ""
            } else {
                body?.item?.last_name.toString()
            }
                nimbblOutputPayload.put(
                    key_user_name,
                    "$firstName $lastName".trim()
                )
            nimbblOutputPayload.put(key_mobileNumber, body?.item?.mobile_number ?: "")
            nimbblOutputPayload.put(key_status, status_success)
            nimbblOutputPayload.put(key_next_step, body?.next_step ?: "payment_mode")
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }


    fun getPaymentEnquiryPayload(
        paymentMode: String,
        message: String,
        status: String,
        orderID: String,
        transactionId: String,
        signature: String
    ): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_paymentEnquiry)
            nimbblOutputPayload.put(key_status, status)
            nimbblOutputPayload.put(key_payment_mode, paymentMode)
            nimbblOutputPayload.put(key_message, message)
            nimbblOutputPayload.put(key_OrderID, orderID)
            nimbblOutputPayload.put(key_transaction_id, transactionId)
            nimbblOutputPayload.put(key_signature, signature)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getInitiatePaymentResponsePayload(
        paymentMode: String,
        status: String,
        transactionId: String
    ): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_initiatePayment)
            nimbblOutputPayload.put(key_status, status)
            nimbblOutputPayload.put(key_payment_mode, paymentMode)
            nimbblOutputPayload.put(key_next, action_completePayment)
            nimbblOutputPayload.put(key_message, value_payment_initiated)
            nimbblOutputPayload.put(key_transaction_id, transactionId)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getCompletePaymentResponsePayload(
        paymentMode: String,
        status: String,
        transactionId: String
    ): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_completePayment)
            nimbblOutputPayload.put(key_status, status)
            nimbblOutputPayload.put(key_payment_mode, paymentMode)
            nimbblOutputPayload.put(key_next, action_completePayment)
            nimbblOutputPayload.put(key_transaction_id, transactionId)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getBinDataResponsePayload(body: BinData): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_action, action_getBinData)
            nimbblOutputPayload.put(key_issuer_name, body.issuingBank)
            nimbblOutputPayload.put(key_issuer_name, body.issuingBank)
            when {
                body.cardCategory.equals("CC",true) -> {
                    nimbblOutputPayload.put(key_sub_payment_code,value_sub_payment_code_credit)
                    nimbblOutputPayload.put(key_sub_payment_name,value_payment_mode_credit_card)
                }
                body.cardCategory.equals("DC",true) -> {
                    nimbblOutputPayload.put(key_sub_payment_code, value_sub_payment_code_debit)
                    nimbblOutputPayload.put(key_sub_payment_name, value_payment_mode_debit_card)
                }
                body.cardCategory.equals("PC",true) -> {
                    nimbblOutputPayload.put(key_sub_payment_code, value_sub_payment_code_prepaid)
                    nimbblOutputPayload.put(key_sub_payment_name, value_payment_mode_prepaid_card)
                }
            }
            nimbblOutputPayload.put(key_scheme_name, body.n_card_type)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getResendOtpPayload(isOtpSent: Boolean): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(key_otp_sent, isOtpSent)
            nimbblOutputPayload.put(key_action, action_resendOtp)
            nimbblOutputPayload.put(key_next, action_completePayment)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getCardValidateDataResponsePayload(): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(status_success, true)
            nimbblOutputPayload.put(key_action, action_validateCard)
            nimbblOutputPayload.put(key_next, action_initiatePayment)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

    fun getUpiIdResponsePayload(
        paymentMode: String,
        vpaValid: Int?,
        vpa: String?,
        status: String?,
        payerAccountName: String?
    ): JSONObject {
        val outputPayload = JSONObject()
        try {
            outputPayload.put(key_event, event_process_result)
            val nimbblOutputPayload = JSONObject()
            nimbblOutputPayload.put(status_success, status)
            nimbblOutputPayload.put(key_action, action_initiatePayment)
            nimbblOutputPayload.put(key_next, action_completePayment)
            nimbblOutputPayload.put(key_payment_mode, paymentMode)
            nimbblOutputPayload.put(key_vpa_id, vpa)
            nimbblOutputPayload.put(key_vpa_valid, vpaValid)
            nimbblOutputPayload.put(key_vpa_account_holder, payerAccountName)
            outputPayload.put(key_nimbblPayload, nimbblOutputPayload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputPayload
    }

}
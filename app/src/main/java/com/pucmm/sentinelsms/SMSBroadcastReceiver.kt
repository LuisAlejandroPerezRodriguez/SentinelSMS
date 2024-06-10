package com.pucmm.sentinelsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SMSBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var onMessageReceived: ((String) -> Unit)? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            val pdus = bundle["pdus"] as Array<*>?
            if (pdus != null) {
                for (pdu in pdus) {
                    val msg = SmsMessage.createFromPdu(pdu as ByteArray)
                    val message = "Number: ${msg.originatingAddress}\nBody: ${msg.messageBody}"
                    Log.d("SMSBroadcastReceiver", "onReceive: $message")
                    onMessageReceived?.invoke(message)
                }
            }
        }
    }
}

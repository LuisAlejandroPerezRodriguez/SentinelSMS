package com.pucmm.sentinelsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

/* This code defines a BroadcastReceiver to listen for incoming SMS messages.
When an SMS is received, it extracts the message details (originating address and body),
logs them, and invokes the onMessageReceived function if it has been set. This allows
other parts of the application to react to incoming SMS messages by defining what the
onMessageReceived function should do with the message. */

class SMSBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var onMessageReceived: ((String) -> Unit)? = null
        /*This is a nullable variable of a function type that takes a String and returns Unit
        (i.e., it takes a String and returns nothing). It's initially set to null.
        This function is intended to be called when an SMS message is received. */
    }
    //This method is overridden from the BroadcastReceiver class. It is called when an SMS message is received
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

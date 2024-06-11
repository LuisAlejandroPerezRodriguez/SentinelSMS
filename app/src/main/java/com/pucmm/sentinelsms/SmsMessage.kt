package com.pucmm.sentinelsms

// This is a simple data model for my incomming SMS messages
data class SmsMessage (
    val id: Long,
    val address: String, // The phone number of the sender
    val body: String, // The message body
    val date: Long, // The timestamp of the message
    val read: Boolean // Whether the message has been read or not
)
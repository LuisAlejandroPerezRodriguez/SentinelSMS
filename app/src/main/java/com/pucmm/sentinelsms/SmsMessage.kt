package com.pucmm.sentinelsms

// This is a simple data model for my incomming SMS messages
data class SmsMessage (
    val id: Long,
    val address: String,
    val body: String,
    val date: Long,
    val read: Boolean,
    val isSent: Boolean
)
package com.pucmm.sentinelsms.Adapter

import com.pucmm.sentinelsms.SmsMessage

data class Conversation(
    val contactName: String,
    val messages: List<SmsMessage>
)
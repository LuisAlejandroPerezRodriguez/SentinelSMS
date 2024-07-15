package com.pucmm.sentinelsms

data class Contact(
    val name: String,
    val phoneNumber: String,
    val initial: Char = name.firstOrNull() ?: ' '
)

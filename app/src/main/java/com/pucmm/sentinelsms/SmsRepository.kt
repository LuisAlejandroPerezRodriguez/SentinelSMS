package com.pucmm.sentinelsms

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.pucmm.sentinelsms.Adapter.Conversation

// This class was created for the purpose of handle the fetching and processing of SMS messages and contacts.
class SmsRepository(private val context: Context) {

    @SuppressLint("Range")
    private fun getContactName(phoneNumber: String): String? {
        val uri: Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return null
    }

    fun fetchConversations(): List<Conversation> {
        val conversations = mutableMapOf<String, MutableList<SmsMessage>>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ
        )
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, Telephony.Sms.DEFAULT_SORT_ORDER)
        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val readIndex = it.getColumnIndex(Telephony.Sms.READ)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)
                val read = it.getInt(readIndex) == 1

                val contactName = getContactName(address) ?: address
                val message = SmsMessage(id, contactName, body, date, read)
                val existingConversation = conversations[contactName]
                if (existingConversation == null || message.date > existingConversation.last().date) {
                    // Either a new conversation or a newer message for an existing conversation
                    conversations[contactName] = mutableListOf(message)
                }
            }
        }
        return conversations.map {Conversation(it.key, it.value) }
    }

    fun fetchMessagesForContact(contactName: String): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ
        )
        val selection = "${Telephony.Sms.ADDRESS} = ?"
        val selectionArgs = arrayOf(contactName) // Assuming contactName is the phone number

        val cursor: Cursor? = context.contentResolver.query(uri, projection, selection, selectionArgs, Telephony.Sms.DEFAULT_SORT_ORDER)
        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val readIndex = it.getColumnIndex(Telephony.Sms.READ)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)
                val read = it.getInt(readIndex) == 1

                messages.add(SmsMessage(id, address, body, date, read))
            }
        }
        return messages
    }
}
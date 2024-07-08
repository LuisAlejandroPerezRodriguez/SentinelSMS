package com.pucmm.sentinelsms

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.sentinelsms.Adapter.SmsAdapter
import android.widget.Toast
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import com.pucmm.sentinelsms.security.AESUtils
import com.pucmm.sentinelsms.security.DHKeyExchange
import com.pucmm.sentinelsms.security.KeyStoreManager
import com.example.securemessaging.SecureMessagingManager
import FirebaseDatabaseManager

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsRepository: SmsRepository
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: Button
    private lateinit var contactNumber: String
    private val smsPermissionRequestCode = 100
    private val myNumber = "8299815535" // Replace with your actual number

    private val secureMessagingManager = SecureMessagingManager(
        KeyStoreManager,
        DHKeyExchange,
        AESUtils,
        FirebaseDatabaseManager()
    )

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            // Fetch new messages and update the adapter
            val messages = smsRepository.fetchMessagesForContact(contactNumber).toMutableList()
            smsAdapter.updateMessages(messages)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.rvChatMessages)
        etMessageInput = findViewById(R.id.etMessageInput)
        btnSend = findViewById(R.id.btnSend)
        recyclerView.layoutManager = LinearLayoutManager(this)
        smsRepository = SmsRepository(this)

        contactNumber = intent.getStringExtra("contactNumber") ?: ""
        val messages = smsRepository.fetchMessagesForContact(contactNumber).toMutableList()
        smsAdapter = SmsAdapter(messages, myNumber)
        recyclerView.adapter = smsAdapter
        // Scroll to the bottom after setting the adapter and layout manager
        recyclerView.scrollToPosition(smsAdapter.itemCount - 1)

        btnSend.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    smsPermissionRequestCode
                )
            } else {
                sendSmsMessage()
            }
        }
        // Register the content observer
        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the content observer
        contentResolver.unregisterContentObserver(smsObserver)
    }

    private fun sendSmsMessage() {
        val message = etMessageInput.text.toString()
        if (message.isNotEmpty()) {
            secureMessagingManager.initiateSecureConversation(myNumber, contactNumber) { secureConversation ->
                if (secureConversation) {
                    secureMessagingManager.sendMessage(myNumber, contactNumber, message) { success ->
                        if (success) {
                            etMessageInput.text.clear()
                            val newMessage = SmsMessage(
                                System.currentTimeMillis(),
                                myNumber,
                                message,
                                System.currentTimeMillis(),
                                true,
                                true
                            ) // Mark as sent
                            smsAdapter.addMessage(newMessage)
                            recyclerView.scrollToPosition(smsAdapter.itemCount - 1)
                        } else {
                            Toast.makeText(this, "Failed to send encrypted message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    sendSms(contactNumber, message)
                    etMessageInput.text.clear()
                    val newMessage = SmsMessage(
                        System.currentTimeMillis(),
                        myNumber,
                        message,
                        System.currentTimeMillis(),
                        true,
                        true
                    ) // Mark as sent
                    smsAdapter.addMessage(newMessage)
                    recyclerView.scrollToPosition(smsAdapter.itemCount - 1)
                }
            }
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun receiveMessages() {
        secureMessagingManager.receiveMessages(myNumber, contactNumber) { decryptedMessages ->
            if (decryptedMessages.isEmpty()) {
                // If there are no decrypted messages, fetch regular SMS messages
                val messages = smsRepository.fetchMessagesForContact(contactNumber).toMutableList()
                smsAdapter.updateMessages(messages)
            } else {
                // Update adapter with decrypted messages
                val smsMessages = decryptedMessages.map { message ->
                    SmsMessage(
                        System.currentTimeMillis(),
                        contactNumber,
                        message,
                        System.currentTimeMillis(),
                        false,
                        false
                    )
                }.toMutableList()
                smsAdapter.updateMessages(smsMessages)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        receiveMessages()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == smsPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSmsMessage()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

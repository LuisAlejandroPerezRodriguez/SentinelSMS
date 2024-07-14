package com.pucmm.sentinelsms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.sentinelsms.Adapter.SmsAdapter
import com.pucmm.sentinelsms.database.FirebaseDatabaseManager
import com.pucmm.sentinelsms.security.CryptoManager
import javax.crypto.spec.SecretKeySpec

class ChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsRepository: SmsRepository
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: Button
    private lateinit var contactNumber: String
    private val smsPermissionRequestCode = 100
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var myNumber: String
    private var secretKey: SecretKeySpec? = null
    private var isSecureConversation = false

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            receiveMessages() // This will fetch  local and Firebase messages
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        myNumber = telephonyManager.line1Number ?: ""

        recyclerView = findViewById(R.id.rvChatMessages)
        etMessageInput = findViewById(R.id.etMessageInput)
        btnSend = findViewById(R.id.btnSend)
        recyclerView.layoutManager = LinearLayoutManager(this)
        smsRepository = SmsRepository(this)

        contactNumber = intent.getStringExtra("contactNumber") ?: ""
        val messages = smsRepository.fetchMessagesForContact(contactNumber).toMutableList()
        smsAdapter = SmsAdapter(messages, myNumber)
        recyclerView.adapter = smsAdapter
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

        contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver)
        initiateSecureConversation()
    }

    private fun initiateSecureConversation() {
        // Ensure contactNumber is in E.164 format
        val formattedContactNumber = if (!contactNumber.startsWith("+")) "+1$contactNumber" else contactNumber
        Log.d("ChatActivity", "Initiating secure conversation with: $formattedContactNumber")

        FirebaseDatabaseManager.getUserPublicKey(formattedContactNumber) { publicKeyBase64 ->
            Log.d("ChatActivity", "Received public key: ${publicKeyBase64 != null}")
            if (publicKeyBase64 != null) {
                val generatedSecretKey = CryptoManager.performKeyExchange(publicKeyBase64)
                Log.d("ChatActivity", "Generated secret key: ${generatedSecretKey != null}")
                if (generatedSecretKey != null) {
                    secretKey = generatedSecretKey
                    isSecureConversation = true
                    runOnUiThread {
                        Toast.makeText(this, "Secure conversation initiated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to initiate secure conversation", Toast.LENGTH_SHORT).show()
                        Log.e("ChatActivity", "Public key not found for contact: $formattedContactNumber")
                    }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Contact not registered for secure messaging", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendSmsMessage() {
        val message = etMessageInput.text.toString()
        if (message.isNotEmpty()) {
            if (isSecureConversation && secretKey != null) {
                sendEncryptedMessage(message)
            } else {
                sendSms(contactNumber, message)
            }
            etMessageInput.text.clear()
            val newMessage = SmsMessage(
                System.currentTimeMillis(),
                myNumber,
                message,
                System.currentTimeMillis(),
                true,
                true
            )
            smsAdapter.addMessage(newMessage)
            recyclerView.scrollToPosition(smsAdapter.itemCount - 1)
        } else {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEncryptedMessage(message: String) {
        val encryptedMessage = CryptoManager.encryptMessage(message, secretKey!!)
        if (encryptedMessage != null) {
            FirebaseDatabaseManager.sendMessage(myNumber, contactNumber, encryptedMessage) { success ->
                runOnUiThread {
                    if (success) {
                        val newMessage = SmsMessage(
                            System.currentTimeMillis(),
                            myNumber,
                            "[Encrypted] $message",
                            System.currentTimeMillis(),
                            true,
                            true
                        )
                        smsAdapter.addMessage(newMessage)
                        recyclerView.scrollToPosition(smsAdapter.itemCount - 1)
                        Toast.makeText(this, "Encrypted message sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send encrypted message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Failed to encrypt message", Toast.LENGTH_SHORT).show()
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
        Log.d("ChatActivity", "Receiving messages")
        val localMessages = smsRepository.fetchMessagesForContact(contactNumber)
        Log.d("ChatActivity", "Local messages count: ${localMessages.size}")

        FirebaseDatabaseManager.getMessagesForUser(myNumber) { firebaseMessages ->
            Log.d("ChatActivity", "Firebase messages count: ${firebaseMessages.size}")
            val decryptedFirebaseMessages = firebaseMessages.mapNotNull { message ->
                if ((message.senderUID == contactNumber || message.senderUID == myNumber) && secretKey != null) {
                    val decryptedContent = CryptoManager.decryptMessage(message.encryptedContent, secretKey!!)
                    if (decryptedContent != null) {
                        SmsMessage(
                            message.timestamp,
                            if (message.senderUID == myNumber) myNumber else contactNumber,
                            "[Encrypted] $decryptedContent",
                            message.timestamp,
                            true,
                            message.senderUID == myNumber
                        )
                    } else null
                } else null
            }
            val allMessages = (localMessages + decryptedFirebaseMessages).sortedBy { it.date }
            runOnUiThread {
                smsAdapter.updateMessages(allMessages.toMutableList())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        receiveMessages()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsObserver)
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

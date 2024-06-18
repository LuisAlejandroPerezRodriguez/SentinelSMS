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



class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsRepository: SmsRepository
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: Button
    private lateinit var contactNumber: String
    private val smsPermissionRequestCode = 100
    private val myNumber = "YourNumberHere" // Replace with your actual number

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
    }

    private fun sendSmsMessage() {
        val message = etMessageInput.text.toString()
        if (message.isNotEmpty()) {
            sendSms(contactNumber, message)
            etMessageInput.text.clear()
            val newMessage = SmsMessage(System.currentTimeMillis(), myNumber, message, System.currentTimeMillis(), true)
            smsAdapter.addMessage(newMessage)
            recyclerView.scrollToPosition(0)
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
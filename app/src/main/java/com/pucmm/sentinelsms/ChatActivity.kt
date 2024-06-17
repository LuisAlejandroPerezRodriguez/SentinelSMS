package com.pucmm.sentinelsms

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pucmm.sentinelsms.Adapter.SmsAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsRepository: SmsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.rvChatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        smsRepository = SmsRepository(this)

        val contactNumber = intent.getStringExtra("contactNumber") ?: ""
        val messages = smsRepository.fetchMessagesForContact(contactNumber)
        smsAdapter = SmsAdapter(messages)
        recyclerView.adapter = smsAdapter
    }
}
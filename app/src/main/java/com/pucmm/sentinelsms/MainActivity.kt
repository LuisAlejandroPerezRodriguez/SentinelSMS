package com.pucmm.sentinelsms

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val PERMISSIONS_REQUEST_READ_SMS = 100
    private var smsList = ArrayList<String>()
    private val TAG = MainActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = findViewById<AppCompatButton>(R.id.send_sms)
        button.setOnClickListener {
            Log.i("boton", "Button clicked")
            val intent = Intent(this, SendMessage::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(smsList)
        recyclerView.adapter = adapter

        // Request SMS permission
        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_SMS
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            showMessages()
            registerSMSBroadcastReceiver()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_SMS),
                PERMISSIONS_REQUEST_READ_SMS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMessages()
                registerSMSBroadcastReceiver()
            } else {
                Snackbar.make(recyclerView, "SMS permission denied", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showMessages() {
        val inboxUri: Uri = Uri.parse("content://sms/inbox")
        smsList.clear()
        val contentResolver = contentResolver
        val cursor = contentResolver.query(inboxUri, null, null, null, null)
        cursor?.let {
            while (it.moveToNext()) {
                val number = it.getString(it.getColumnIndexOrThrow("address"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                smsList.add("Number: $number\nBody: $body")
                Log.d(TAG, "showMessages: Number:$number Body:$body")
            }
        }
        cursor?.close()
        adapter.notifyDataSetChanged()
    }

    private fun registerSMSBroadcastReceiver() {
        val receiver = SMSBroadcastReceiver()
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(receiver, filter)
        SMSBroadcastReceiver.onMessageReceived = { message ->
            runOnUiThread {
                displayMessage(message)
            }
        }
    }

    private fun displayMessage(message: String) {
        smsList.add(message)
        adapter.notifyDataSetChanged()
        recyclerView.scrollToPosition(smsList.size - 1)
    }

    companion object {
        private const val REQUEST_SMS_PERMISSION = 1
    }
}

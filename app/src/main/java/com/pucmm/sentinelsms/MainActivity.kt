package com.pucmm.sentinelsms

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pucmm.sentinelsms.Adapter.ConversationAdapter
import com.pucmm.sentinelsms.Adapter.SmsAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapter: SmsAdapter
    private lateinit var smsRepository: SmsRepository

    private val REQUEST_CODE_PERMISSIONS = 101
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserUid: String
    private var authDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_option1 -> showToast("Option 1")
                R.id.nav_option2 -> showToast("Option 2")
                R.id.nav_option3 -> showToast("Option 3")
            }
            drawerLayout.closeDrawers()
            true
        }

        // Check and request permissions
        if (!hasPermissions()) {
            requestPermissions()
        } else {
            initialize()
            setupAuth()
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.READ_CONTACTS
        ), REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initialize()
                setupAuth()
            } else {
                // Permissions not granted. Handle appropriately.
            }
        }
    }

    private fun initialize() {
        smsRepository = SmsRepository(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch conversations and display them
        val conversations = smsRepository.fetchConversations()
        val conversationAdapter = ConversationAdapter(conversations)
        recyclerView.adapter = conversationAdapter
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupAuth() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            showAuthDialog()
        } else {
            // User is already signed in
            // Proceed with your app logic
            initializeApp()
        }
    }

    private fun showAuthDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
        dialog.setContentView(R.layout.dialog_auth)
        dialog.setCancelable(false)

        authDialog = dialog // Initialize the authDialog variable

        // Set up click listeners for the buttons
        val etEmail = dialog.findViewById<EditText>(R.id.etEmail)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val btnSignIn = dialog.findViewById<Button>(R.id.btnSignIn)
        val btnSignUp = dialog.findViewById<Button>(R.id.btnSignUp)

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmailAndPassword(email, password)
                dialog.dismiss()
            } else {
                showToast("Please enter email and password")
            }
        }

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                createUserWithEmailAndPassword(email, password)
                dialog.dismiss()
            } else {
                showToast("Please enter email and password")
            }
        }

        dialog.show()
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    runOnUiThread {
                        showToast("Sign in successful")
                        currentUserUid = user?.uid ?: ""
                        initializeApp()
                    }
                } else {
                    runOnUiThread {
                        showToast("Sign in failed: ${task.exception?.message}")
                    }
                }
            }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    runOnUiThread {
                        showToast("Sign up successful")
                        currentUserUid = user?.uid ?: ""
                        initializeApp()
                    }
                } else {
                    runOnUiThread {
                        showToast("Sign up failed: ${task.exception?.message}")
                    }
                }
            }
    }


    private fun initializeApp() {
        // Initialize your app logic and UI here
        // For example, you can show the RecyclerView and other UI elements
        recyclerView.visibility = View.VISIBLE

        // Dismiss the authentication dialog
        authDialog?.dismiss()
        authDialog = null
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        // Update the UI based on the user's authentication state
        // For example, you can show/hide certain UI elements or navigate to different screens
        if (currentUser == null) {
            // User is not signed in, show the authentication dialog
            showAuthDialog()
        } else {
            // User is signed in, initialize the app
            initializeApp()
        }
    }
}

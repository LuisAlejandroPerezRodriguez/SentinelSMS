package com.pucmm.sentinelsms

import android.app.Dialog
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pucmm.sentinelsms.database.FirebaseDatabaseManager
import com.pucmm.sentinelsms.security.CryptoManager

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var smsRepository: SmsRepository
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var authDialog: Dialog? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = Firebase.database
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()

        setupNavigationDrawer()

        if (!hasPermissions()) {
            requestPermissions()
        } else {
            initialize()
            setupAuth()
        }

        auth = FirebaseAuth.getInstance()

        if (!hasPermissions()) {
            requestPermissions()
        } else {
            initialize()
            setupAuth()
        }
    }

    private fun setupNavigationDrawer() {
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
        // Dismiss the existing dialog if it's showing
        authDialog?.dismiss()

        val dialog = Dialog(this, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
        dialog.setContentView(R.layout.dialog_auth)
        dialog.setCancelable(false)

        authDialog = dialog // Initialize the authDialog variable

        // Set up click listeners for the buttons
        val etEmail = dialog.findViewById<EditText>(R.id.etEmail)
        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val etPhoneNumber = dialog.findViewById<EditText>(R.id.etPhoneNumber)
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
            val phoneNumber = etPhoneNumber.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && phoneNumber.isNotEmpty()) {
                createUserWithEmailAndPassword(email, password, phoneNumber)
                dialog.dismiss()
            } else {
                showToast("Please enter email, password, and phone number")
            }
        }

        dialog.show()
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        runOnUiThread {
                            showToast("Sign in successful")
                            onSuccessfulAuth(user.uid)
                            initializeApp()
                            authDialog?.dismiss()
                            authDialog = null
                        }
                    }
                } else {
                    runOnUiThread {
                        showToast("Sign in failed: ${task.exception?.message}")
                    }
                }
            }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String, phoneNumber: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                runOnUiThread {
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            showToast("Sign up successful")
                            setupUserCrypto(user.uid, phoneNumber)
                            onSuccessfulAuth(user.uid)
                            signInWithEmailAndPassword(email, password)
                        }
                    } else {
                        showToast("Sign up failed: ${task.exception?.message}")
                    }
                }
            }
    }

    private fun setupUserCrypto(userId: String, phoneNumber: String) {
        val formattedPhoneNumber = if (!phoneNumber.startsWith("+")) "+1$phoneNumber" else phoneNumber
        val publicKeyBase64 = CryptoManager.generateAndStoreKeyPair()
        if (publicKeyBase64 != null) {
            FirebaseDatabaseManager.saveUserData(userId, formattedPhoneNumber, publicKeyBase64) { success ->
                if (success) {
                    Log.d("MainActivity", "Public key saved successfully for user: $userId")
                } else {
                    Log.e("MainActivity", "Failed to save public key for user: $userId")
                    showToast("Failed to set up secure messaging. Please try again later.")
                }
            }
        } else {
            Log.e("MainActivity", "Failed to generate key pair for user: $userId")
            showToast("Failed to set up secure messaging. Please try again later.")
        }
    }

    // Call this function after successful login or registration
    private fun onSuccessfulAuth(userId: String) {
        // You can add more post-authentication logic here
        Log.d("MainActivity", "Authentication successful for user: $userId")
    }


    private fun initializeApp() {
        // Initialize your app logic and UI here
        // For example, you can show the RecyclerView and other UI elements
        recyclerView.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onSuccessfulAuth(currentUser.uid)
            updateUI(currentUser)
        } else {
            showAuthDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dismiss the authentication dialog
        authDialog?.dismiss()
        authDialog = null
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

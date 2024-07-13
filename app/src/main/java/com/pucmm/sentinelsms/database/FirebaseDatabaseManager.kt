package com.pucmm.sentinelsms.database

import com.google.firebase.database.*
import java.util.logging.Level
import java.util.logging.Logger

data class User(
    val phoneNumber: String = "",
    val publicKey: String = ""
)

data class Message(
    val senderUID: String = "",
    val recipientUID: String = "",
    val encryptedContent: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object FirebaseDatabaseManager {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val logger: Logger = Logger.getLogger(FirebaseDatabaseManager::class.java.name)

    fun saveUserData(userId: String, phoneNumber: String, publicKey: String, callback: (Boolean) -> Unit) {
        if (userId.isEmpty() || phoneNumber.isEmpty() || publicKey.isEmpty()) {
            logger.log(Level.WARNING, "UserId, PhoneNumber, or PublicKey is empty")
            callback(false)
            return
        }

        val user = User(phoneNumber, publicKey)
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                logger.log(Level.SEVERE, "Failed to save user data", exception)
                callback(false)
            }
    }

    fun getUserPublicKey(phoneNumber: String, callback: (String?) -> Unit) {
        database.child("users").orderByChild("phoneNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val publicKey = snapshot.children.firstOrNull()?.child("publicKey")?.value as? String
                    callback(publicKey)
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.log(Level.SEVERE, "Failed to retrieve public key", error.toException())
                    callback(null)
                }
            })
    }

    fun sendMessage(senderUID: String, recipientUID: String, encryptedContent: String, callback: (Boolean) -> Unit) {
        if (senderUID.isEmpty() || recipientUID.isEmpty() || encryptedContent.isEmpty()) {
            logger.log(Level.WARNING, "SenderUID, RecipientUID, or EncryptedContent is empty")
            callback(false)
            return
        }

        val message = Message(senderUID, recipientUID, encryptedContent)
        database.child("messages").push().setValue(message)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                logger.log(Level.SEVERE, "Failed to send message", exception)
                callback(false)
            }
    }

    fun getMessagesForUser(userUID: String, callback: (List<Message>) -> Unit) {
        database.child("messages")
            .orderByChild("recipientUID")
            .equalTo(userUID)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    snapshot.children.forEach { child ->
                        val message = child.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    callback(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.log(Level.SEVERE, "Failed to retrieve messages", error.toException())
                    callback(emptyList())
                }
            })
    }

    fun getUserIdByPhoneNumber(phoneNumber: String, callback: (String?) -> Unit) {
        database.child("users").orderByChild("phoneNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userId = snapshot.children.firstOrNull()?.key
                    callback(userId)
                }

                override fun onCancelled(error: DatabaseError) {
                    logger.log(Level.SEVERE, "Failed to retrieve user ID", error.toException())
                    callback(null)
                }
            })
    }
}

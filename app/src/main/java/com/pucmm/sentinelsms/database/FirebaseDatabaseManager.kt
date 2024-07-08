import com.google.firebase.database.*
import java.util.logging.Level
import java.util.logging.Logger

data class User(
    val userId: String = "",
    val publicKey: String = ""
)

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val encryptedMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class FirebaseDatabaseManager {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val logger: Logger = Logger.getLogger(FirebaseDatabaseManager::class.java.name)

    fun registerUser(userId: String, publicKey: String, callback: (Boolean) -> Unit) {
        if (userId.isEmpty() || publicKey.isEmpty()) {
            logger.log(Level.WARNING, "UserId or PublicKey is empty")
            callback(false)
            return
        }

        val user = User(userId, publicKey)
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                logger.log(Level.SEVERE, "Failed to register user", exception)
                callback(false)
            }
    }

    fun getUserPublicKey(userId: String, callback: (String?) -> Unit) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user?.publicKey)
            }

            override fun onCancelled(error: DatabaseError) {
                logger.log(Level.SEVERE, "Failed to retrieve public key", error.toException())
                callback(null)
            }
        })
    }

    fun sendMessage(senderId: String, receiverId: String, encryptedMessage: String, callback: (Boolean) -> Unit) {
        if (senderId.isEmpty() || receiverId.isEmpty() || encryptedMessage.isEmpty()) {
            logger.log(Level.WARNING, "SenderId, ReceiverId, or EncryptedMessage is empty")
            callback(false)
            return
        }

        val message = Message(senderId, receiverId, encryptedMessage)
        database.child("messages").child(senderId).child(receiverId).push().setValue(message)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                logger.log(Level.SEVERE, "Failed to send message", exception)
                callback(false)
            }
    }

    fun getMessagesForUser(userId: String, callback: (List<Message>) -> Unit) {
        database.child("messages").child(userId).limitToLast(100).addListenerForSingleValueEvent(object : ValueEventListener {
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
}

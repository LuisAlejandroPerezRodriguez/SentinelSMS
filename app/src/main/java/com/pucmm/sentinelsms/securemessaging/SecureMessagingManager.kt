package com.example.securemessaging

import FirebaseDatabaseManager
import android.util.Base64
import com.pucmm.sentinelsms.security.AESUtils
import com.pucmm.sentinelsms.security.DHKeyExchange
import com.pucmm.sentinelsms.security.KeyStoreManager
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class SecureMessagingManager(
    private val keyStoreManager: KeyStoreManager,
    private val dhKeyExchange: DHKeyExchange,
    private val aesUtils: AESUtils,
    private val firebaseDatabaseManager: FirebaseDatabaseManager
) {

    fun registerUser(userId: String, callback: (Boolean) -> Unit) {
        val keyPair = keyStoreManager.generateKeyPair() ?: return callback(false)
        val publicKeyBytes = keyPair.public.encoded
        val publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.DEFAULT)
        firebaseDatabaseManager.registerUser(userId, publicKeyBase64) { success ->
            callback(success)
        }
    }

    fun initiateSecureConversation(senderId: String, receiverId: String, callback: (Boolean) -> Unit) {
        firebaseDatabaseManager.getUserPublicKey(receiverId) { recipientPublicKeyBase64 ->
            if (recipientPublicKeyBase64 != null) {
                val recipientPublicKeyBytes = Base64.decode(recipientPublicKeyBase64, Base64.DEFAULT)
                val recipientPublicKey = dhKeyExchange.bytesToPublicKey(recipientPublicKeyBytes) ?: return@getUserPublicKey callback(false)
                val keyPair = keyStoreManager.getKeyPair() ?: return@getUserPublicKey callback(false)
                val sharedSecret = dhKeyExchange.generateSharedSecret(keyPair.private, recipientPublicKey) ?: return@getUserPublicKey callback(false)
                val secretKeySpec = SecretKeySpec(sharedSecret, 0, sharedSecret.size, "AES")
                keyStoreManager.storeSecretKey("$senderId-$receiverId", secretKeySpec)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, message: String, callback: (Boolean) -> Unit) {
        firebaseDatabaseManager.getUserPublicKey(receiverId) { recipientPublicKeyBase64 ->
            if (recipientPublicKeyBase64 != null) {
                // Receiver is a registered user, send encrypted message
                val secretKey = keyStoreManager.getSecretKey("$senderId-$receiverId")
                if (secretKey != null) {
                    val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")
                    val encryptedMessage = aesUtils.encrypt(secretKeySpec, message.toByteArray(Charsets.UTF_8))?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                    if (encryptedMessage != null) {
                        firebaseDatabaseManager.sendMessage(senderId, receiverId, encryptedMessage) { success ->
                            callback(success)
                        }
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            } else {
                // Receiver is not a registered user, send plain text message
                firebaseDatabaseManager.sendMessage(senderId, receiverId, message) { success ->
                    callback(success)
                }
            }
        }
    }

    fun receiveMessages(userId: String, senderId: String, callback: (List<String>) -> Unit) {
        firebaseDatabaseManager.getMessagesForUser(userId) { messages ->
            val secretKey = keyStoreManager.getSecretKey("$userId-$senderId")
            if (secretKey != null) {
                val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")
                val decryptedMessages = messages.mapNotNull { message ->
                    val encryptedMessageBytes = Base64.decode(message.encryptedMessage, Base64.DEFAULT)
                    aesUtils.decrypt(secretKeySpec, encryptedMessageBytes, null)?.toString(Charsets.UTF_8)
                }
                callback(decryptedMessages)
            } else {
                callback(emptyList())
            }
        }
    }
}

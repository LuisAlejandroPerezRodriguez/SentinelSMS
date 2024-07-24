package com.pucmm.sentinelsms.security

import android.util.Log
import java.security.KeyPair
import java.security.PublicKey
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val TAG = "CryptoManager"

    fun generateAndStoreKeyPair(): String? {
        val keyPair = KeystoreManager.generateKeyPair()
        return if (keyPair != null) {
            DHKeyExchange.publicKeyToBase64(keyPair.public)
        } else {
            Log.e(TAG, "Failed to generate key pair")
            null
        }
    }


    fun performKeyExchange(otherPublicKeyBase64: String): SecretKeySpec? {
        val otherPublicKey = DHKeyExchange.base64ToPublicKey(otherPublicKeyBase64)
        val privateKey = KeystoreManager.getPrivateKey()

        if (otherPublicKey == null || privateKey == null) {
            Log.e(TAG, "Failed to retrieve keys for key exchange")
            return null
        }

        val sharedSecret = DHKeyExchange.generateSharedSecret(privateKey, otherPublicKey)
        return if (sharedSecret != null) {
            AESUtils.generateSecretKeyFromBytes(sharedSecret)
        } else {
            Log.e(TAG, "Failed to generate shared secret")
            null
        }
    }

    fun encryptMessage(message: String, secretKey: SecretKeySpec): String? {
        return try {
            AESUtils.encrypt(secretKey, message.toByteArray())
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            null
        }
    }

    fun decryptMessage(encryptedMessage: String, secretKey: SecretKeySpec): String? {
        return try {
            val decryptedBytes = AESUtils.decrypt(secretKey, encryptedMessage)
            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            null
        }
    }
}
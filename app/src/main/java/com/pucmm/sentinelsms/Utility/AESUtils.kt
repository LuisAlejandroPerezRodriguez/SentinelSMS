package com.pucmm.sentinelsms.security

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Log

object AESUtils {

    private const val TAG = "AESUtils"
    private const val AES_ALGORITHM = "AES"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    fun generateSecretKeyFromBytes(keyBytes: ByteArray): SecretKeySpec {
        return SecretKeySpec(keyBytes, AES_ALGORITHM)
    }

    fun encrypt(secretKey: SecretKey, data: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            cipher.doFinal(data)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data", e)
            null
        }
    }

    fun decrypt(secretKey: SecretKey, encryptedData: ByteArray, gcmParameterSpec: GCMParameterSpec? = null): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data", e)
            null
        }
    }
}

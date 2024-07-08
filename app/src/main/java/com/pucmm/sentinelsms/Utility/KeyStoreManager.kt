package com.pucmm.sentinelsms.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.ECGenParameterSpec
import android.util.Log
import javax.crypto.SecretKey

object KeyStoreManager {

    private const val KEY_ALIAS = "SentinelDHKeyPair"
    private const val TAG = "KeyStoreManager"

    fun generateKeyPair(): KeyPair? {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setAlgorithmParameterSpec(
                        ECGenParameterSpec("secp256r1")
                    )
                    .build()
            )
            keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key pair", e)
            null
        }
    }

    fun getKeyPair(): KeyPair? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
            val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey
            if (privateKey != null && publicKey != null) {
                KeyPair(publicKey, privateKey)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving key pair", e)
            null
        }
    }

    fun storeSecretKey(alias: String, secretKey: SecretKey) {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.setEntry(alias, KeyStore.SecretKeyEntry(secretKey), null)
        } catch (e: Exception) {
            Log.e(TAG, "Error storing secret key", e)
        }
    }

    fun getSecretKey(alias: String): SecretKey? {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKeyEntry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            return secretKeyEntry?.secretKey
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving secret key", e)
            return null
        }
    }
}

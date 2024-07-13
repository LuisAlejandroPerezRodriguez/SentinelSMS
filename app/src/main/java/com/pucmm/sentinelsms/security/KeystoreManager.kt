package com.pucmm.sentinelsms.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.*
import java.security.spec.ECGenParameterSpec

object KeystoreManager {
    private const val KEY_ALIAS = "DH_KEY_ALIAS"
    private const val EC_CURVE = "secp256r1"

    fun generateKeyPair(): KeyPair? {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_AGREE_KEY
            ).setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                .setUserAuthenticationRequired(false)
                .build()

            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            Log.e("KeystoreManager", "Error generating key pair", e)
            null
        }
    }

    fun getPrivateKey(): PrivateKey? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.getKey(KEY_ALIAS, null) as PrivateKey
        } catch (e: Exception) {
            Log.e("KeystoreManager", "Error getting private key", e)
            null
        }
    }

    fun getPublicKey(): PublicKey? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.getCertificate(KEY_ALIAS).publicKey
        } catch (e: Exception) {
            Log.e("KeystoreManager", "Error getting public key", e)
            null
        }
    }
}

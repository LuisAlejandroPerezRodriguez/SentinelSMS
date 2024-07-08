package com.pucmm.sentinelsms.security

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import android.util.Log
import java.security.KeyFactory
import java.security.PrivateKey

object DHKeyExchange {

    private const val TAG = "DHKeyExchange"

    fun generateKeyPair(): KeyPair? {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("EC")
            keyPairGenerator.initialize(256)
            keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key pair", e)
            null
        }
    }

    fun generateSharedSecret(privateKey: PrivateKey, publicKey: PublicKey): ByteArray? {
        return try {
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(privateKey)
            keyAgreement.doPhase(publicKey, true)
            keyAgreement.generateSecret()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating shared secret", e)
            null
        }
    }

    fun publicKeyToBytes(publicKey: PublicKey): ByteArray {
        return publicKey.encoded
    }

    fun bytesToPublicKey(bytes: ByteArray): PublicKey? {
        return try {
            val keyFactory = KeyFactory.getInstance("EC")
            keyFactory.generatePublic(X509EncodedKeySpec(bytes))
        } catch (e: Exception) {
            Log.e(TAG, "Error converting bytes to public key", e)
            null
        }
    }
}

package com.pucmm.sentinelsms.security

import android.util.Base64
import android.util.Log
import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

object DHKeyExchange {
    private const val TAG = "DHKeyExchange"

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

    fun publicKeyToBase64(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    fun base64ToPublicKey(base64Key: String): PublicKey? {
        return try {
            val keyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance("EC")
            keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
        } catch (e: Exception) {
            Log.e(TAG, "Error converting base64 to public key", e)
            null
        }
    }
}

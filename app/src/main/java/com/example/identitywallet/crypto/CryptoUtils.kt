package com.example.identitywallet.crypto

import android.util.Base64
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Fonctions de chiffrement/déchiffrement (AES-GCM) et signature/vérification (ECDSA).
 * Les clés utilisées viennent toujours du Keystore (KeystoreManager), jamais manipulées en clair.
 */
object CryptoUtils {

    private const val GCM_TAG_LENGTH_BITS = 128

    /**
     * Résultat d'un chiffrement : le texte chiffré + l'IV (nécessaire pour déchiffrer).
     * On encode tout en Base64 pour pouvoir le stocker facilement en String (SharedPreferences).
     */
    data class EncryptedData(val ciphertextB64: String, val ivB64: String)

    // ---------- CHIFFREMENT / DÉCHIFFREMENT (AES-GCM) ----------

    fun encrypt(plainText: String, key: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv // généré automatiquement par le Cipher
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return EncryptedData(
            ciphertextB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP),
            ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun decrypt(encryptedData: EncryptedData, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(encryptedData.ivB64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val ciphertext = Base64.decode(encryptedData.ciphertextB64, Base64.NO_WRAP)
        val plainBytes = cipher.doFinal(ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    // ---------- SIGNATURE / VÉRIFICATION (ECDSA) ----------

    fun sign(data: String, privateKey: java.security.PrivateKey): String {
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray(Charsets.UTF_8))
        val signatureBytes = signature.sign()
        return Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
    }

    fun verify(data: String, signatureB64: String, publicKey: java.security.PublicKey): Boolean {
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(publicKey)
        signature.update(data.toByteArray(Charsets.UTF_8))
        val signatureBytes = Base64.decode(signatureB64, Base64.NO_WRAP)
        return signature.verify(signatureBytes)
    }
}
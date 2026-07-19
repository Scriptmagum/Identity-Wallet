package com.example.identitywallet.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Gère la création et la récupération des clés cryptographiques
 * stockées dans l'Android Keystore (jamais exposées en clair, jamais exportables).
 */
object KeystoreManager {

    private const val PROVIDER = "AndroidKeyStore"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(PROVIDER).apply { load(null) }
    }

    /**
     * Crée (ou récupère si elle existe déjà) une paire de clés ECDSA P-256
     * pour un credential donné, protégée par authentification biométrique.
     * Alias unique par credential, ex: "cred_abc123_ecdsa"
     */
    fun getOrCreateSigningKey(alias: String): KeyStore.Entry {
        if (!keyStore.containsAlias(alias)) {
            val generator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(30)
                .build()
            generator.initialize(spec)
            generator.generateKeyPair()
        }
        return keyStore.getEntry(alias, null)
    }

    /**
     * Crée (ou récupère) une clé AES-256-GCM pour chiffrer les données
     * d'un credential, protégée par authentification biométrique.
     * Alias unique par credential, ex: "cred_abc123_aes"
     */
    fun getOrCreateEncryptionKey(alias: String): SecretKey {
        if (!keyStore.containsAlias(alias)) {
            val generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(30)
                .build()
            generator.init(spec)
            generator.generateKey()
        }
        return keyStore.getKey(alias, null) as SecretKey
    }

    fun getPublicKey(alias: String): PublicKey {
        return keyStore.getCertificate(alias).publicKey
    }

    fun getPrivateKey(alias: String): PrivateKey {
        return keyStore.getKey(alias, null) as PrivateKey
    }

    fun deleteKey(alias: String) {
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }
}
package com.example.identitywallet.data

import android.content.Context
import com.example.identitywallet.crypto.CryptoUtils
import com.example.identitywallet.crypto.KeystoreManager
import com.example.identitywallet.crypto.MockCA
import com.example.identitywallet.model.CredentialType
import com.example.identitywallet.model.IdentityData
import com.example.identitywallet.model.QrPayload
import java.util.UUID

/**
 * Façade unique qui combine crypto + CA + stockage.
 * L'UI ne parle jamais directement au Keystore, à MockCA ou au CredentialStorage —
 * elle passe toujours par ce Repository.
 */
class CredentialRepository(context: Context) {

    private val storage = CredentialStorage(context)

    /**
     * Étape "création" complète :
     * 1. Génère les clés (ECDSA + AES) pour ce nouveau credential
     * 2. Chiffre les données d'identité
     * 3. Fait certifier la clé publique par la CA
     * 4. Stocke tout
     */
    fun createCredential(type: CredentialType, identityData: IdentityData): CredentialEntity {
        val id = UUID.randomUUID().toString()
        val ecdsaAlias = "cred_${id}_ecdsa"
        val aesAlias = "cred_${id}_aes"

        // 1. Génération des clés dans le Keystore
        KeystoreManager.getOrCreateSigningKey(ecdsaAlias)
        val aesKey = KeystoreManager.getOrCreateEncryptionKey(aesAlias)

        // 2. Chiffrement des données
        val encrypted = CryptoUtils.encrypt(identityData.toJson(), aesKey)

        // 3. Certification de la clé publique par la CA
        val publicKey = KeystoreManager.getPublicKey(ecdsaAlias)
        val certificate = MockCA.issueCertificate(publicKey)

        // 4. Stockage
        val entity = CredentialEntity(
            id = id,
            type = type,
            ciphertextB64 = encrypted.ciphertextB64,
            ivB64 = encrypted.ivB64,
            certificate = certificate,
            createdAt = System.currentTimeMillis()
        )
        storage.add(entity)
        return entity
    }

    fun getAll(): List<CredentialEntity> = storage.loadAll()

    fun getById(id: String): CredentialEntity? = storage.loadAll().find { it.id == id }

    fun deleteById(id: String) {
        storage.deleteById(id)
        KeystoreManager.deleteKey("cred_${id}_ecdsa")
        KeystoreManager.deleteKey("cred_${id}_aes")
    }

    /**
     * Déchiffre les données d'un credential.
     * ⚠️ Doit être appelé APRÈS authentification biométrique réussie
     * (sinon le Keystore lève une exception UserNotAuthenticatedException).
     */
    fun decryptIdentityData(entity: CredentialEntity): IdentityData {
        val aesAlias = "cred_${entity.id}_aes"
        val aesKey = KeystoreManager.getOrCreateEncryptionKey(aesAlias)
        val encrypted = CryptoUtils.EncryptedData(entity.ciphertextB64, entity.ivB64)
        val json = CryptoUtils.decrypt(encrypted, aesKey)
        return IdentityData.fromJson(json)
    }

    /**
     * Construit le QR de présentation : déchiffre, signe, assemble le payload.
     * ⚠️ Doit aussi être appelé APRÈS authentification biométrique réussie.
     */
    fun buildQrPayload(entity: CredentialEntity): QrPayload {
        val identityData = decryptIdentityData(entity)
        val timestamp = System.currentTimeMillis()
        val nonce = UUID.randomUUID().toString()

        val payloadWithoutSignature = QrPayload(
            identityData = identityData,
            timestamp = timestamp,
            nonce = nonce,
            userSignatureB64 = "", // pas encore signé
            certificate = entity.certificate
        )

        val ecdsaAlias = "cred_${entity.id}_ecdsa"
        val privateKey = KeystoreManager.getPrivateKey(ecdsaAlias)
        val signature = CryptoUtils.sign(payloadWithoutSignature.signedContent(), privateKey)

        return payloadWithoutSignature.copy(userSignatureB64 = signature)
    }
}
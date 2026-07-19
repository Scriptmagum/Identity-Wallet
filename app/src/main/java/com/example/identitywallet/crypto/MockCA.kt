package com.example.identitywallet.crypto

import android.util.Base64
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

/**
 * Simule une Autorité de Certification (CA) — ce que ferait normalement
 * un backend distant (Go/Python). Elle possède SA PROPRE paire de clés
 * (différente de celles des utilisateurs) et signe les clés publiques
 * des credentials pour attester qu'ils sont "certifiés".
 *
 * ⚠️ Ici la clé privée CA vit en mémoire dans l'app pour la démo.
 * Dans un vrai système, elle serait uniquement côté serveur, jamais sur le device.
 */
object MockCA {

    // Générées une seule fois au lancement de l'app (in-memory, pas persistées)
    private val caKeyPair = KeyPairGenerator.getInstance("EC").apply {
        initialize(256)
    }.generateKeyPair()

    val caPublicKey: PublicKey get() = caKeyPair.public
    private val caPrivateKey: PrivateKey get() = caKeyPair.private

    /**
     * Simule l'appel POST /register : la CA reçoit une clé publique
     * de credential et la signe pour attester qu'elle est "certifiée".
     */
    fun issueCertificate(subjectPublicKey: PublicKey): Certificate {
        val issuedAt = System.currentTimeMillis()
        val expiresAt = issuedAt + (365L * 24 * 60 * 60 * 1000) // valide 1 an

        val subjectKeyB64 = Base64.encodeToString(subjectPublicKey.encoded, Base64.NO_WRAP)
        val dataToSign = "$subjectKeyB64|$issuedAt|$expiresAt"

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(caPrivateKey)
        signature.update(dataToSign.toByteArray(Charsets.UTF_8))
        val caSignatureB64 = Base64.encodeToString(signature.sign(), Base64.NO_WRAP)

        return Certificate(
            subjectPublicKeyB64 = subjectKeyB64,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            caSignatureB64 = caSignatureB64
        )
    }

    /**
     * Simule la vérification qu'un vérificateur ferait avec la clé publique
     * CA "embarquée" dans son app — ici on l'a directement car tout vit
     * dans la même app pour la démo.
     */
    fun verifyCertificate(certificate: Certificate): Boolean {
        val dataToVerify = "${certificate.subjectPublicKeyB64}|${certificate.issuedAt}|${certificate.expiresAt}"

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(caPublicKey)
        signature.update(dataToVerify.toByteArray(Charsets.UTF_8))

        val signatureBytes = Base64.decode(certificate.caSignatureB64, Base64.NO_WRAP)
        val signatureValid = signature.verify(signatureBytes)

        val notExpired = System.currentTimeMillis() < certificate.expiresAt

        return signatureValid && notExpired
    }
}

/**
 * Certificat émis par la CA : atteste qu'une clé publique donnée
 * est "certifiée", sans rien connaître des données d'identité associées.
 */
data class Certificate(
    val subjectPublicKeyB64: String,
    val issuedAt: Long,
    val expiresAt: Long,
    val caSignatureB64: String
)
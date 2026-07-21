package com.example.identitywallet.crypto

import android.util.Base64
import com.example.identitywallet.model.IdentityData
import com.example.identitywallet.model.QrPayload
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Vérifie un QR scanné : validité du certificat CA, signature utilisateur,
 * et fraîcheur du timestamp (anti-rejeu).
 */
object QrVerifier {

    private const val MAX_AGE_MS = 120_000L // 2 minutes — doit matcher le minuteur affiché à l'émission

    sealed class Result {
        data class Valid(val identityData: IdentityData) : Result()
        data class Invalid(val reason: String) : Result()
    }

    fun verify(qrJson: String): Result {
        val payload: QrPayload = try {
            QrPayload.fromJson(qrJson)
        } catch (e: Exception) {
            return Result.Invalid("QR illisible ou mal formé")
        }

        // 1. Le certificat est-il valide (signé par la CA + non expiré) ?
        if (!MockCA.verifyCertificate(payload.certificate)) {
            return Result.Invalid("Certificat invalide ou expiré")
        }

        // 2. Reconstruire la clé publique du porteur à partir du certificat
        val subjectPublicKey = try {
            val keyBytes = Base64.decode(payload.certificate.subjectPublicKeyB64, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance("EC")
            keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
        } catch (e: Exception) {
            return Result.Invalid("Clé publique illisible")
        }

        // 3. La signature de l'utilisateur correspond-elle à ces données précises ?
        val signatureValid = try {
            CryptoUtils.verify(payload.signedContent(), payload.userSignatureB64, subjectPublicKey)
        } catch (e: Exception) {
            false
        }
        if (!signatureValid) {
            return Result.Invalid("Signature invalide — données modifiées ou falsifiées")
        }

        // 4. Le QR est-il encore frais (anti-rejeu) ?
        val age = System.currentTimeMillis() - payload.timestamp
        if (age > MAX_AGE_MS) {
            return Result.Invalid("QR expiré — redemandez une présentation")
        }

        return Result.Valid(payload.identityData)
    }
}
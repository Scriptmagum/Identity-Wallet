package com.example.identitywallet.model

import com.example.identitywallet.crypto.Certificate
import org.json.JSONObject

/**
 * Ce qui est encodé dans le QR code au moment de la présentation :
 * les données + une preuve de fraîcheur (timestamp/nonce) + la signature
 * de l'utilisateur + le certificat émis par la CA.
 */
data class QrPayload(
    val identityData: IdentityData,
    val timestamp: Long,
    val nonce: String,
    val userSignatureB64: String,
    val certificate: Certificate
) {
    /**
     * La chaîne exacte qui a été signée par l'utilisateur — doit être
     * reconstruite à l'identique pour vérifier la signature plus tard.
     */
    fun signedContent(): String {
        return "${identityData.toJson()}|$timestamp|$nonce"
    }

    fun toJson(): String {
        return JSONObject().apply {
            put("identityData", JSONObject(identityData.toJson()))
            put("timestamp", timestamp)
            put("nonce", nonce)
            put("userSignatureB64", userSignatureB64)
            put("certificate", JSONObject().apply {
                put("subjectPublicKeyB64", certificate.subjectPublicKeyB64)
                put("issuedAt", certificate.issuedAt)
                put("expiresAt", certificate.expiresAt)
                put("caSignatureB64", certificate.caSignatureB64)
            })
        }.toString()
    }

    companion object {
        fun fromJson(json: String): QrPayload {
            val obj = JSONObject(json)
            val identityObj = obj.getJSONObject("identityData")
            val certObj = obj.getJSONObject("certificate")

            return QrPayload(
                identityData = IdentityData.fromJson(identityObj.toString()),
                timestamp = obj.getLong("timestamp"),
                nonce = obj.getString("nonce"),
                userSignatureB64 = obj.getString("userSignatureB64"),
                certificate = Certificate(
                    subjectPublicKeyB64 = certObj.getString("subjectPublicKeyB64"),
                    issuedAt = certObj.getLong("issuedAt"),
                    expiresAt = certObj.getLong("expiresAt"),
                    caSignatureB64 = certObj.getString("caSignatureB64")
                )
            )
        }
    }
}
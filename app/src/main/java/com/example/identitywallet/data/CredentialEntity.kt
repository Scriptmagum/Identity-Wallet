package com.example.identitywallet.data

import com.example.identitywallet.crypto.Certificate
import com.example.identitywallet.model.CredentialType

/**
 * Ce qui est réellement stocké sur le disque (SharedPreferences).
 * Les champs sensibles (ciphertextB64) sont déjà chiffrés avant d'arriver ici.
 */
data class CredentialEntity(
    val id: String,
    val type: CredentialType,
    val ciphertextB64: String,
    val ivB64: String,
    val certificate: Certificate,
    val createdAt: Long
)
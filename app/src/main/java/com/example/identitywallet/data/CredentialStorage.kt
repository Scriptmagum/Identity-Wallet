package com.example.identitywallet.data

import android.content.Context
import com.example.identitywallet.crypto.Certificate
import com.example.identitywallet.model.CredentialType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistance simple via SharedPreferences : une liste JSON de credentials.
 * Rappel : ce qui est stocké ici est DÉJÀ chiffré (ciphertextB64), donc
 * même en clair dans le fichier XML, illisible sans la clé du Keystore.
 */
class CredentialStorage(context: Context) {

    private val prefs = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)
    private val KEY_CREDENTIALS = "credentials_list"

    fun loadAll(): List<CredentialEntity> {
        val json = prefs.getString(KEY_CREDENTIALS, null) ?: return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            entityFromJson(array.getJSONObject(i))
        }
    }

    fun saveAll(credentials: List<CredentialEntity>) {
        val array = JSONArray()
        credentials.forEach { array.put(entityToJson(it)) }
        prefs.edit().putString(KEY_CREDENTIALS, array.toString()).apply()
    }

    fun add(credential: CredentialEntity) {
        val current = loadAll().toMutableList()
        current.add(credential)
        saveAll(current)
    }

    fun deleteById(id: String) {
        val current = loadAll().filter { it.id != id }
        saveAll(current)
    }

    // ---------- Conversion JSON ----------

    private fun entityToJson(entity: CredentialEntity): JSONObject {
        return JSONObject().apply {
            put("id", entity.id)
            put("type", entity.type.name)
            put("ciphertextB64", entity.ciphertextB64)
            put("ivB64", entity.ivB64)
            put("createdAt", entity.createdAt)
            put("certificate", JSONObject().apply {
                put("subjectPublicKeyB64", entity.certificate.subjectPublicKeyB64)
                put("issuedAt", entity.certificate.issuedAt)
                put("expiresAt", entity.certificate.expiresAt)
                put("caSignatureB64", entity.certificate.caSignatureB64)
            })
        }
    }

    private fun entityFromJson(obj: JSONObject): CredentialEntity {
        val certObj = obj.getJSONObject("certificate")
        return CredentialEntity(
            id = obj.getString("id"),
            type = CredentialType.valueOf(obj.getString("type")),
            ciphertextB64 = obj.getString("ciphertextB64"),
            ivB64 = obj.getString("ivB64"),
            createdAt = obj.getLong("createdAt"),
            certificate = Certificate(
                subjectPublicKeyB64 = certObj.getString("subjectPublicKeyB64"),
                issuedAt = certObj.getLong("issuedAt"),
                expiresAt = certObj.getLong("expiresAt"),
                caSignatureB64 = certObj.getString("caSignatureB64")
            )
        )
    }
}
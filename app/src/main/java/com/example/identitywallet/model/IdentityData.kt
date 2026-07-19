package com.example.identitywallet.model

import org.json.JSONObject

/**
 * Les données brutes d'un document d'identité (avant chiffrement).
 * Ce qui est réellement chiffré et stocké — jamais en clair sur le disque.
 */
data class IdentityData(
    val nom: String,
    val prenom: String,
    val dateNaissance: String,
    val nationalite: String,
    val numeroDocument: String
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("nom", nom)
            put("prenom", prenom)
            put("dateNaissance", dateNaissance)
            put("nationalite", nationalite)
            put("numeroDocument", numeroDocument)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): IdentityData {
            val obj = JSONObject(json)
            return IdentityData(
                nom = obj.getString("nom"),
                prenom = obj.getString("prenom"),
                dateNaissance = obj.getString("dateNaissance"),
                nationalite = obj.getString("nationalite"),
                numeroDocument = obj.getString("numeroDocument")
            )
        }
    }
}
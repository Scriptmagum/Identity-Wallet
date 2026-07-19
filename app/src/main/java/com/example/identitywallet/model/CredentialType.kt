package com.example.identitywallet.model

/**
 * Les types de documents que le wallet peut stocker.
 * displayName = ce qui s'affiche à l'utilisateur.
 */
enum class CredentialType(val displayName: String) {
    CARTE_IDENTITE("Carte d'identité"),
    PERMIS_CONDUIRE("Permis de conduire"),
    CARTE_ETUDIANT("Carte étudiant")
}
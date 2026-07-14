
# Identity Wallet — Android (Kotlin / Jetpack Compose)

## Aperçu

Portefeuille d'identité numérique Android. Stocke une identité fictive chiffrée localement, déverrouillable par biométrie, présentable via QR Code signé.

---

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  UI (Compose)   │────▶│  ViewModel       │────▶│  Repository     │
│  - Formulaire   │     │  - État app      │     │  - Keystore     │
│  - QR Code      │     │  - Logique métier│     │  - Chiffrement  │
│  - Vérification │     │                  │     │  - Signature    │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                          │
                                                          ▼
                                                  ┌─────────────────┐
                                                  │  Android Keystore│
                                                  │  (TEE / StrongBox)│
                                                  └─────────────────┘
```

---

## Fonctionnalités

- **Création d'identité** (JSON) → chiffrée AES-GCM → stockée localement
- **Déverrouillage biométrique** (BiometricPrompt)
- **Présentation via QR Code** : `{ data, signature ECDSA, certificat }`
- **Vérification de signature** (mode lecteur)

---

## Sécurité

| Mécanisme | Rôle | Techno |
|-----------|------|--------|
| Chiffrement | Confidentialité des données | AES-256 GCM |
| Stockage des clés | Protection des clés | Android Keystore (TEE/StrongBox) |
| Biométrie | Authentification utilisateur | BiometricPrompt |
| Signature | Intégrité du QR Code | ECDSA P-256 |

---

## Stack technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose, Material 3
- **Architecture** : MVVM, Coroutines, StateFlow
- **Sécurité** : Android Keystore, BiometricPrompt, AES-GCM, ECDSA (Bouncy Castle / Conscrypt)
- **Stockage** : Room (SQLite) / DataStore

---

## Tests

| Type | Outil | Cible |
|------|-------|-------|
| Unitaires | JUnit + MockK | ViewModel + Repository |
| UI | Compose UI Test | Écrans formulaire et QR |
| Intégration | Instrumented tests | Keystore + chiffrement |

---

## Limites / Améliorations

> Version simplifiée (POC). Dans un déploiement réel :

- [ ] La clé publique serait certifiée par une autorité (backend)
- [ ] Le QR embarquerait un certificat signé (X.509)
- [ ] Les données seraient synchronisées entre appareils
- [ ] Ajout d'un mécanisme de révocation
- [ ] Conformité EUDI Wallet (ARF/ARC)

---

## Installation

```bash
# 1. Cloner le repository
git clone https://github.com/Scriptmagum/identity-wallet-android.git

# 2. Ouvrir dans Android Studio
# File > Open > sélectionner le dossier

# 3. Lancer sur un appareil
# API 28+ (Android 9+) avec biométrie disponible
```

---

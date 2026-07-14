# Identity Wallet — Android (Kotlin / Jetpack Compose)

## Aperçu
Portefeuille d'identité numérique Android. Stocke une identité fictive
chiffrée localement, déverrouillable par biométrie, présentable via QR Code signé.

## Architecture
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

## Fonctionnalités
- Création d'identité (JSON) → chiffrée AES-GCM → stockée localement
- Déverrouillage biométrique (BiometricPrompt)
- Présentation via QR Code : { data, signature ECDSA, certificat }
- Vérification de signature (mode lecteur)

## Sécurité
│ Mécanisme           │ Rôle                          │ Techno              │
│─────────────────────│───────────────────────────────│─────────────────────│
│ Chiffrement         │ Confidentialité des données   │ AES-256 GCM         │
│ Stockage des clés   │ Protection des clés           │ Android Keystore    │
│ Biométrie           │ Authentification utilisateur  │ BiometricPrompt     │
│ Signature           │ Intégrité du QR Code          │ ECDSA P-256         │

## Stack technique
- Kotlin, Jetpack Compose, Material 3
- MVVM, Coroutines, StateFlow
- Android Keystore, BiometricPrompt
- AES-GCM, ECDSA (Bouncy Castle / Conscrypt)

## Tests
- Tests unitaires (JUnit) — ViewModel + Repository
- Tests UI (Compose) — écrans formulaire et QR

## Limites / Améliorations
Version simplifiée. Dans un déploiement réel :
- La clé publique serait certifiée par une autorité (backend)
- Le QR embarquerait un certificat signé
- Les données seraient synchronisées entre appareils

## Installation
1. Cloner le repo
2. Ouvrir dans Android Studio
3. Lancer sur un appareil API 28+ (biométrie nécessaire)


# Identity Wallet — Android (Kotlin / View System)

## Aperçu

Portefeuille d'identité numérique Android. Stocke une identité fictive chiffrée localement, déverrouillable par biométrie, présentable via QR Code signé.

---

## Architecture

```
┌──────────────────────────────┐
│       Activities (UI)        │
│  MainActivity (liste)        │
│  CreateCredentialActivity    │
│  QrDisplayActivity           │
└──────┬───────────────────────┘
       │ appel
┌──────▼───────────────────────┐
│    CredentialRepository      │  ← Façade unique
│    (data layer)              │
└──┬───────┬────────┬─────────┘
   │       │        │
   ▼       ▼        ▼
Keystore  Crypto   Credential
Manager   Utils    Storage
(Android  (AES-    (SharedPrefs
 Keystore  GCM +    → JSON
 TEE/      ECDSA)   déjà chiffré)
 StrongBox)
   │
   ▼
 MockCA (mémoire)
```

---

## Fonctionnalités

- **Création d'identité** (JSON) → chiffrée AES-GCM → stockée localement
- **Déverrouillage biométrique** (BiometricPrompt)
- **Présentation via QR Code** (ZXing) : `{ data, timestamp, nonce, signature ECDSA, certificat }`
- **Compte à rebours** de 2 min sur le QR (anti-rejeu)

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
- **UI** : View System (XML layouts), AppCompatActivity, ListView, Material FAB
- **Architecture** : Activities + Repository + Keystore
- **Sécurité** : Android Keystore, BiometricPrompt, AES-GCM, ECDSA
- **Stockage** : SharedPreferences (JSON sérialisé, données déjà chiffrées)
- **QR Code** : ZXing (com.google.zxing + journeyapps barcode scanner)

---

## Tests

| Type | Outil | Cible |
|------|-------|-------|
| Unitaires | JUnit + MockK | Repository |
| UI | Espresso | Activities |
| Intégration | Instrumented tests | Keystore + chiffrement |

---

## Limites / Améliorations

> Version simplifiée (POC). Dans un déploiement réel :

- [ ] La clé privée CA ne serait pas en mémoire dans l'app
- [ ] Le QR embarquerait un vrai certificat X.509 (pas un format custom)
- [ ] Les données seraient synchronisées entre appareils
- [ ] Ajout d'un mécanisme de révocation
- [ ] Conformité EUDI Wallet (ARF/ARC)
- [ ] Migrer vers Jetpack Compose + ViewModel + Room

---

## Installation

```bash
# 1. Cloner le repository
git clone https://github.com/Scriptmagum/Identity-Wallet.git

# 2. Ouvrir dans Android Studio
# File > Open > sélectionner le dossier

# 3. Lancer sur un appareil
# API 28+ (Android 9+) avec biométrie disponible
```

---

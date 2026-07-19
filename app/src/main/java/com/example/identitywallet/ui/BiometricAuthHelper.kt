package com.example.identitywallet.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Déclenche le popup d'authentification (empreinte/visage/PIN) et exécute
 * un callback de succès ou d'erreur. Une fois réussi, les clés Keystore
 * configurées avec setUserAuthenticationValidityDurationSeconds(30) deviennent
 * utilisables pendant 30 secondes, peu importe quelle opération crypto les utilise.
 */
object BiometricAuthHelper {

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                // Empreinte non reconnue — l'utilisateur peut réessayer, pas une erreur fatale
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentification requise")
            .setSubtitle("Confirmez votre identité pour continuer")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
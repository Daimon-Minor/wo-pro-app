package com.wopro.app.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Biometric helper — used to unlock the app / approve sensitive actions.
 * Fails gracefully when no biometric hardware/enrollment exists.
 */
object BiometricHelper {

    fun isAvailable(context: Context): Boolean {
        val result = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Verify identity",
        subtitle: String = "Use your fingerprint or face to continue",
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!isAvailable(activity)) {
            onError("Biometric not available or not enrolled")
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
                override fun onAuthenticationFailed() {
                    // user cancelled or failed; ignore for UX
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        prompt.authenticate(promptInfo)
    }
}

package com.itwg.mundial.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
class BiometricHelper(
    private val activity: FragmentActivity,
) {
    private val executor = ContextCompat.getMainExecutor(activity)

    private val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun availability(): BiometricAvailability {
        return when (BiometricManager.from(activity).canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotEnrolled
            else -> BiometricAvailability.Unavailable
        }
    }

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {},
    ) {
        when (val status = availability()) {
            BiometricAvailability.NoHardware -> {
                onError("Este dispositivo no tiene lector de huella.")
                return
            }
            BiometricAvailability.NotEnrolled -> {
                onError("Registra una huella en Ajustes del teléfono primero.")
                return
            }
            BiometricAvailability.Unavailable -> {
                onError("No se pudo verificar la huella.")
                return
            }
            BiometricAvailability.Available -> Unit
        }

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onCancel()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    // El lector puede fallar un intento; el usuario puede reintentar.
                }
            },
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear Mundial")
            .setSubtitle("Usa tu huella para continuar")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(info)
    }
}

sealed class BiometricAvailability {
    data object Available : BiometricAvailability()
    data object NoHardware : BiometricAvailability()
    data object NotEnrolled : BiometricAvailability()
    data object Unavailable : BiometricAvailability()
}

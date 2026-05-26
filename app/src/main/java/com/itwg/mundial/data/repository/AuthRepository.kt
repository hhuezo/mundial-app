package com.itwg.mundial.data.repository

import android.content.Context
import com.itwg.mundial.data.api.ApiClient
import com.itwg.mundial.data.model.LoginRequest
import com.itwg.mundial.data.model.LoginResponse
import com.itwg.mundial.data.model.ResetPasswordRequest
import com.itwg.mundial.data.model.StoredUserProfile
import com.itwg.mundial.data.model.UserDto
import com.itwg.mundial.data.model.UserSession
import com.itwg.mundial.data.store.CredentialStore
import com.itwg.mundial.data.store.SessionStore
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {
    private val sessionStore = SessionStore(context)
    private val credentialStore = CredentialStore(context)
    private val authApi = ApiClient.authApi

    suspend fun getSession(): UserSession? = sessionStore.getSession()

    suspend fun getStoredUserProfile(): StoredUserProfile? = sessionStore.getStoredUserProfile()

    suspend fun restoreSession(): Boolean {
        val session = sessionStore.getSession() ?: return false
        ApiClient.setBearerToken(session.token)
        return true
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(
                LoginRequest(
                    email = email.trim(),
                    password = password,
                ),
            )
            credentialStore.saveCredentials(email, password)
            ApiClient.setBearerToken(null)
            sessionStore.replaceSession(response.toSession())
            ApiClient.setBearerToken(response.token)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor. Revise su conexión."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al iniciar sesión."))
        }
    }

    /** Obtiene un token nuevo en el servidor tras validar la huella. */
    suspend fun loginWithStoredCredentials(): Result<LoginResponse> {
        val (email, password) = credentialStore.getCredentials()
            ?: return Result.failure(
                Exception("No hay credenciales guardadas. Inicia sesión con tu correo."),
            )
        return login(email, password)
    }

    /**
     * Cierra la sesión activa: revoca el token en el servidor, lo borra del dispositivo
     * y mantiene perfil + credenciales cifradas para el próximo inicio con huella.
     */
    suspend fun lockSession() {
        val session = sessionStore.getSession()
        if (session != null) {
            ApiClient.setBearerToken(session.token)
            try {
                authApi.logout()
            } catch (_: Exception) {
                // Se elimina el token local aunque falle la revocación remota.
            }
        }
        sessionStore.clearToken()
        ApiClient.setBearerToken(null)
    }

    /** Revoca el token y borra perfil, credenciales y preferencias. */
    suspend fun clearSessionAndLogout() {
        lockSession()
        sessionStore.clearAll()
        credentialStore.clearCredentials()
    }

    suspend fun hasActiveToken(): Boolean = sessionStore.hasActiveToken()

    suspend fun hasStoredCredentials(): Boolean = credentialStore.hasCredentials()

    suspend fun isBiometricEnabled(): Boolean = sessionStore.isBiometricEnabled()

    suspend fun setBiometricEnabled(enabled: Boolean) {
        sessionStore.setBiometricEnabled(enabled)
    }

    suspend fun shouldShowBiometricLock(): Boolean =
        sessionStore.isBiometricEnabled() && credentialStore.hasCredentials()

    suspend fun currentUser(): UserDto? {
        return try {
            authApi.me().user
        } catch (_: Exception) {
            null
        }
    }

    suspend fun resetPassword(userId: Long, password: String): Result<String> {
        return try {
            val response = authApi.resetPassword(
                ResetPasswordRequest(userId = userId, password = password),
            )
            credentialStore.getCredentials()?.let { (email, _) ->
                credentialStore.saveCredentials(email, password)
            }
            Result.success(response.message)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor. Revise su conexión."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al actualizar la contraseña."))
        }
    }

    private fun LoginResponse.toSession() = UserSession(
        userId = user.id,
        token = token,
        userName = user.name,
        unidadId = user.unidadId,
        unidadNombre = user.unidad?.nombre,
    )

    private fun parseHttpError(exception: HttpException): String {
        val body = exception.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try {
                val validation = ApiClient.validationErrorAdapter.fromJson(body)
                validation?.errors?.values?.flatten()?.firstOrNull()?.let { return it }
                validation?.message?.let { return it }
            } catch (_: Exception) {
                // Respuesta no JSON de validación.
            }
        }
        return when (exception.code()) {
            401, 403 -> "No autorizado."
            422 -> "Datos inválidos. Revise correo y contraseña."
            in 500..599 -> "Error en el servidor. Intente más tarde."
            else -> "Error al iniciar sesión (${exception.code()})."
        }
    }
}

package com.itwg.mundial.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.itwg.mundial.data.model.StoredUserProfile
import com.itwg.mundial.data.model.UserSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class SessionStore(context: Context) {
    private val dataStore = context.applicationContext.authDataStore

    suspend fun getSession(): UserSession? {
        val prefs = dataStore.data.first()
        val token = prefs[KEY_TOKEN] ?: return null
        val userId = prefs[KEY_USER_ID] ?: return null
        return UserSession(
            userId = userId,
            token = token,
            unidadId = prefs[KEY_UNIDAD_ID],
            unidadNombre = prefs[KEY_UNIDAD_NOMBRE],
        )
    }

    suspend fun getStoredUserProfile(): StoredUserProfile? {
        val prefs = dataStore.data.first()
        val userId = prefs[KEY_USER_ID] ?: return null
        return StoredUserProfile(
            userId = userId,
            unidadId = prefs[KEY_UNIDAD_ID],
            unidadNombre = prefs[KEY_UNIDAD_NOMBRE],
        )
    }

    suspend fun saveSession(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = session.token
            prefs[KEY_USER_ID] = session.userId
            session.unidadId?.let { prefs[KEY_UNIDAD_ID] = it } ?: prefs.remove(KEY_UNIDAD_ID)
            session.unidadNombre?.let { prefs[KEY_UNIDAD_NOMBRE] = it } ?: prefs.remove(KEY_UNIDAD_NOMBRE)
        }
    }

    /** Borra solo el token; conserva id, unidad y preferencia de huella. */
    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_UNIDAD_ID)
            prefs.remove(KEY_UNIDAD_NOMBRE)
            prefs.remove(KEY_BIOMETRIC_ENABLED)
        }
    }

    suspend fun hasActiveToken(): Boolean =
        dataStore.data.map { it[KEY_TOKEN] != null }.first()

    suspend fun hasStoredUser(): Boolean =
        dataStore.data.map { it[KEY_USER_ID] != null }.first()

    suspend fun isBiometricEnabled(): Boolean =
        dataStore.data.map { it[KEY_BIOMETRIC_ENABLED] == true }.first()

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            if (enabled) {
                prefs[KEY_BIOMETRIC_ENABLED] = true
            } else {
                prefs.remove(KEY_BIOMETRIC_ENABLED)
            }
        }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_UNIDAD_ID = longPreferencesKey("unidad_id")
        private val KEY_UNIDAD_NOMBRE = stringPreferencesKey("unidad_nombre")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }
}

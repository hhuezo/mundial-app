package com.itwg.mundial.data.repository

import android.content.Context
import com.itwg.mundial.data.api.ApiClient
import com.itwg.mundial.data.model.HomeData
import com.itwg.mundial.data.store.SessionStore
import retrofit2.HttpException
import java.io.IOException

class HomeRepository(context: Context) {
    private val homeApi = ApiClient.homeApi
    private val sessionStore = SessionStore(context)

    suspend fun loadHome(userId: Long): Result<HomeData> {
        return try {
            val session = sessionStore.getSession()
                ?: return Result.failure(Exception("No hay sesión activa."))
            ApiClient.setBearerToken(session.token)
            val response = homeApi.getHome(userId = userId)
            Result.success(
                HomeData(
                    faces = response.faces,
                    grupos = response.grupos,
                ),
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Error al cargar home (${e.code()})."))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al cargar home."))
        }
    }
}

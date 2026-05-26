package com.itwg.mundial.data.repository

import android.content.Context
import com.itwg.mundial.data.api.ApiClient
import com.itwg.mundial.data.mapper.filterByGroup
import com.itwg.mundial.data.store.SessionStore
import com.itwg.mundial.data.mapper.resolveGroups
import com.itwg.mundial.data.model.MarcadorDetailResponse
import com.itwg.mundial.data.model.MarcadoresFaceDto
import com.itwg.mundial.data.model.PartidoDto
import com.itwg.mundial.data.model.UpdateMarcadorRequest
import com.itwg.mundial.model.MatchPrediction
import com.itwg.mundial.ui.home.FASE_GRUPOS_ID
import retrofit2.HttpException
import java.io.IOException

data class MarcadoresData(
    val faces: List<MarcadoresFaceDto>,
    val grupos: List<String>?,
)

class MarcadoresRepository(
    context: Context? = null,
) {
    private val marcadoresApi = ApiClient.marcadoresApi
    private val sessionStore = context?.let { SessionStore(it) }

    private suspend fun ensureBearerToken(): Result<Unit> {
        val store = sessionStore ?: return Result.success(Unit)
        val session = store.getSession()
            ?: return Result.failure(Exception("No hay sesión activa."))
        ApiClient.setBearerToken(session.token)
        return Result.success(Unit)
    }

    suspend fun loadMarcadores(userId: Long, faseId: Long? = null): Result<MarcadoresData> {
        return try {
            ensureBearerToken().getOrElse { return Result.failure(it) }
            val response = marcadoresApi.getMarcadores(userId = userId, faseId = faseId)
            Result.success(
                MarcadoresData(
                    faces = response.faces,
                    grupos = response.grupos,
                ),
            )
        } catch (e: HttpException) {
            Result.failure(Exception("Error al cargar marcadores (${e.code()})."))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al cargar marcadores."))
        }
    }

    fun groupsForFace(face: MarcadoresFaceDto, apiGrupos: List<String>?): List<String> {
        if (face.faseId != FASE_GRUPOS_ID) return emptyList()
        return resolveGroups(apiGrupos, face.partidos)
    }

    fun matchesForGroup(partidos: List<PartidoDto>, group: String): List<MatchPrediction> =
        partidos.filterByGroup(group)

    suspend fun loadMarcadorDetail(partidoId: Long, userId: Long): Result<MarcadorDetailResponse> {
        return try {
            ensureBearerToken().getOrElse { return Result.failure(it) }
            val response = marcadoresApi.getMarcadorDetail(partidoId = partidoId, userId = userId)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception("Error al cargar el partido (${e.code()})."))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al cargar el partido."))
        }
    }

    suspend fun updateMarcador(
        userId: Long,
        unidadId: Long,
        partidoId: Long,
        marcadorPais1: Int,
        marcadorPais2: Int,
    ): Result<Unit> {
        return try {
            marcadoresApi.updateMarcador(
                partidoId = partidoId,
                body = UpdateMarcadorRequest(
                    userId = userId,
                    unidadId = unidadId,
                    marcadorPais1 = marcadorPais1,
                    marcadorPais2 = marcadorPais2,
                ),
            )
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(parseUpdateMarcadorError(e)))
        } catch (_: IOException) {
            Result.failure(Exception("No se pudo conectar con el servidor."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al guardar marcador."))
        }
    }

    private fun parseUpdateMarcadorError(exception: HttpException): String {
        val body = exception.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try {
                val validation = ApiClient.validationErrorAdapter.fromJson(body)
                validation?.message?.let { return it }
                validation?.errors?.values?.flatten()?.firstOrNull()?.let { return it }
            } catch (_: Exception) {
                // Respuesta no JSON de validación.
            }
        }
        return when (exception.code()) {
            422 -> "No se pudo guardar el marcador."
            401, 403 -> "No autorizado."
            in 500..599 -> "Error en el servidor. Intente más tarde."
            else -> "Error al guardar marcador (${exception.code()})."
        }
    }
}

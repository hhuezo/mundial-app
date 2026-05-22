package com.itwg.mundial.data.repository

import com.itwg.mundial.data.api.ApiClient
import com.itwg.mundial.data.mapper.availableGroups
import com.itwg.mundial.data.mapper.filterByGroup
import com.itwg.mundial.data.model.PartidoDto
import com.itwg.mundial.model.MatchPrediction
import retrofit2.HttpException
import java.io.IOException

data class MarcadoresData(
    val groups: List<String>,
    val partidos: List<PartidoDto>,
)

class MarcadoresRepository {
    private val marcadoresApi = ApiClient.marcadoresApi

    suspend fun loadMarcadores(userId: Long, faseId: Long? = null): Result<MarcadoresData> {
        return try {
            val response = marcadoresApi.getMarcadores(userId = userId, faseId = faseId)
            val groupsFromPartidos = response.partidos.availableGroups()
            val groups = when {
                groupsFromPartidos.isNotEmpty() && !response.grupos.isNullOrEmpty() ->
                    response.grupos.filter { it in groupsFromPartidos.toSet() }
                        .ifEmpty { groupsFromPartidos }
                !response.grupos.isNullOrEmpty() -> response.grupos
                else -> groupsFromPartidos
            }
            Result.success(
                MarcadoresData(
                    groups = groups,
                    partidos = response.partidos,
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

    fun matchesForGroup(partidos: List<PartidoDto>, group: String): List<MatchPrediction> =
        partidos.filterByGroup(group)
}

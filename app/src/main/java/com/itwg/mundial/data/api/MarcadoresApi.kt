package com.itwg.mundial.data.api

import com.itwg.mundial.data.model.MarcadorDetailResponse
import com.itwg.mundial.data.model.MarcadoresResponse
import com.itwg.mundial.data.model.MessageResponse
import com.itwg.mundial.data.model.UpdateMarcadorRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MarcadoresApi {
    @GET("marcadores")
    suspend fun getMarcadores(
        @Query("userId") userId: Long,
        @Query("fase_id") faseId: Long? = null,
    ): MarcadoresResponse

    @GET("marcadores/{marcadore}")
    suspend fun getMarcadorDetail(
        @Path("marcadore") partidoId: Long,
        @Query("userId") userId: Long,
    ): MarcadorDetailResponse

    @PUT("marcadores/{partidoId}")
    suspend fun updateMarcador(
        @Path("partidoId") partidoId: Long,
        @Body body: UpdateMarcadorRequest,
    ): MessageResponse
}

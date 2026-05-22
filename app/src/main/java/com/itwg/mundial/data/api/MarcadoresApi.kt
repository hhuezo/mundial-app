package com.itwg.mundial.data.api

import com.itwg.mundial.data.model.MarcadoresResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MarcadoresApi {
    @GET("marcadores")
    suspend fun getMarcadores(
        @Query("userId") userId: Long,
        @Query("fase_id") faseId: Long? = null,
    ): MarcadoresResponse
}

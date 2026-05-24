package com.itwg.mundial.data.api

import com.itwg.mundial.data.model.HomeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("home")
    suspend fun getHome(
        @Query("userId") userId: Long,
    ): HomeResponse
}

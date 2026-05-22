package com.itwg.mundial.data.api

import com.itwg.mundial.data.model.LoginRequest
import com.itwg.mundial.data.model.LoginResponse
import com.itwg.mundial.data.model.MeResponse
import com.itwg.mundial.data.model.MessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("logout")
    suspend fun logout(): MessageResponse

    @GET("me")
    suspend fun me(): MeResponse
}

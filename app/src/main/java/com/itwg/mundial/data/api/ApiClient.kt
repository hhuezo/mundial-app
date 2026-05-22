package com.itwg.mundial.data.api

import com.itwg.mundial.config.ApiConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var bearerToken: String? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val headersInterceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
        bearerToken?.let { builder.header("Authorization", "Bearer $it") }
        chain.proceed(builder.build())
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(headersInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            },
        )
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    val marcadoresApi: MarcadoresApi by lazy { retrofit.create(MarcadoresApi::class.java) }

    val validationErrorAdapter =
        moshi.adapter(com.itwg.mundial.data.model.ValidationErrorResponse::class.java)

    fun setBearerToken(token: String?) {
        bearerToken = token
    }
}

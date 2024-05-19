package com.example.project.models

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MailApiClient {
    private const val BASE_URL = "https://mail-api-jwtg.onrender.com"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

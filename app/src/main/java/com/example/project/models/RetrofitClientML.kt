package com.example.project.models

import com.example.project.prototype.ApiServiceML
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientML {
    private const val BASE_URL = "http://127.0.0.1:5000"

    private val httpClient = OkHttpClient.Builder().build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    val apiService: ApiServiceML = retrofit.create(ApiServiceML::class.java)
}

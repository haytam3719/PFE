package com.example.project.models

import com.example.project.prototype.ApiAgencesService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAgencesInstance {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://gsrv.itafrica.mobi/geoserver/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiAgencesService::class.java)
}

package com.example.project.prototype

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiServiceML {

    @Multipart
    @POST("/upload_reference")
    fun uploadReference(@Part file: MultipartBody.Part): Call<ResponseBody>

    @GET("/trancher")
    fun getFinalDecision(): Call<FinalDecisionResponse>
}

data class FinalDecisionResponse(
    val final_decision: String
)

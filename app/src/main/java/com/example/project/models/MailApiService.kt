package com.example.project.models

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class EmailRequest(val email: String)
data class EmailResponse(val message: String)

interface MailApiService {
    @Headers("Content-Type: application/json")
    @POST("/send-email")
    fun sendEmail(@Body request: EmailRequest): Call<EmailResponse>
}

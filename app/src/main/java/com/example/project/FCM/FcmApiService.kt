package com.example.project.FCM

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST



interface FcmApiService {
    @POST("send-notification")
    fun sendNotification(@Body data: NotificationData): Call<Void>
}

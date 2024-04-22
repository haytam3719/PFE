package com.example.project.repositories

import android.util.Log
import com.example.project.models.RetrofitClient
import com.example.project.prototype.OAuthService
import javax.inject.Inject

class OAuthRepository @Inject constructor(retrofit: RetrofitClient) {

    private val oAuthService = retrofit.retrofit.create(OAuthService::class.java)

    suspend fun getAccessToken(clientId: String, clientSecret: String): String {
        try {
            Log.d(TAG, "Attempting to get access token...")
            val response = oAuthService.getAccessToken("client_credentials", clientId, clientSecret)
            if (response.error != null) {
                throw Exception(response.error)
            }
            Log.d(TAG, "Access token received: ${response.accessToken}")
            return response.accessToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "OAuthRepository"
    }
}

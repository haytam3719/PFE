package com.example.project.prototype

import com.example.project.models.AgenceResponse
import com.example.project.models.TypeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiAgencesService {
    @GET("geoloc_getPOI.php")
    suspend fun getAgencesNear(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("action") action: String
    ): AgenceResponse

    @GET("geoloc_getTypePOI.php?action=gettypes")
    suspend fun getTypes(): TypeResponse
}

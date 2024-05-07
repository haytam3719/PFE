package com.example.project.repositories

import android.util.Log
import com.example.project.models.Agence
import com.example.project.models.Type
import com.example.project.prototype.AgencesRepository
import com.example.project.prototype.ApiAgencesService

import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class AgencesRepositoryImpl @Inject constructor(private val apiService: ApiAgencesService) : AgencesRepository {

    override suspend fun getAgencesNear(latitude: Double, longitude: Double, action: String): List<Agence> {
        return try {
            val response = apiService.getAgencesNear(latitude, longitude, action)
            Log.d("API Response", "Action: $action, Agences: ${response.agence.size}")

            response.agence ?: emptyList()
        } catch (e: Exception) {
            Log.e("API Error", "Failed to fetch agences for action: $action", e)
            emptyList()
        }
    }

    override suspend fun getTypes(): List<Type> {
        return try {
            val response = apiService.getTypes()
            response.types ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }


     override suspend fun getAgencesWithinRadius(latitude: Double, longitude: Double, radius: Double, action: String): List<Agence> {
        val lat = latitude.toDouble()
        val lon = longitude.toDouble()

        val allAgencies = getAgencesNear(lat, lon, action)

        return allAgencies.filter { calculateDistance(lat, lon, it.latitude.toDouble(), it.longitude.toDouble()) <= radius }
    }

    fun Double.toRadians(): Double = this * (PI / 180)

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2).pow(2) + cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return earthRadius * c * 1000
    }
}

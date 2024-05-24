package com.example.project.prototype

import com.example.project.models.Agence
import com.example.project.models.GAB
import com.example.project.models.Type

interface AgencesRepository {
    suspend fun getAgencesNear(latitude: Double, longitude: Double, action: String): List<Agence>
    suspend fun getTypes(): List<Type>
    suspend fun getAgencesWithinRadius(
        latitude: Double,
        longitude: Double,
        radius: Double,
        action: String
    ): List<Agence>

    suspend fun getGABNear(latitude: Double, longitude: Double, action: String): List<GAB>
    suspend fun getGABWithinRadius(latitude: Double, longitude: Double, radius: Double, action: String): List<GAB>
}
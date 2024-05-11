package com.example.project.prototype

import com.example.project.models.DeviceInfo

interface ClientRepository {
    suspend fun getClientDevices(clientId: String): List<DeviceInfo>?
}
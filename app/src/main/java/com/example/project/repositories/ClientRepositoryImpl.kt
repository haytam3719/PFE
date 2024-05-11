package com.example.project.repositories

import android.util.Log
import com.example.project.models.DeviceInfo
import com.example.project.prototype.ClientRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await

class ClientRepositoryImpl : ClientRepository {
    private val databaseReference = FirebaseDatabase.getInstance().getReference("clients")
    override suspend fun getClientDevices(clientUid: String): List<DeviceInfo>? {
        val databaseReference = FirebaseDatabase.getInstance().getReference("clients")

        return try {
            Log.d("getClientDevices", "Querying for client UID: $clientUid")
            val snapshot = databaseReference.orderByChild("Client/uid").equalTo(clientUid).get().await()

            Log.d("getClientDevices", "Query completed. Snapshot exists: ${snapshot.exists()}")
            if (!snapshot.exists()) {
                Log.d("getClientDevices", "No data found for client UID: $clientUid")
                return null
            }

            val deviceInfoList = mutableListOf<DeviceInfo>()
            snapshot.children.forEach { child ->
                Log.d("getClientDevices", "Processing client with key: ${child.key}")

                // Log and add all devices from deviceInfoList if present
                child.child("Client/deviceInfoList").children.forEach { deviceChild ->
                    deviceChild.getValue<DeviceInfo>()?.also { deviceInfo ->
                        deviceInfoList.add(deviceInfo)
                        Log.d("getClientDevices", "Added DeviceInfo from list: $deviceInfo")
                    }
                }
            }

            if (deviceInfoList.isEmpty()) {
                Log.d("getClientDevices", "Device info list is empty after processing snapshot")
                null
            } else {
                Log.d("getClientDevices", "Device info list populated with ${deviceInfoList.size} devices")
                deviceInfoList
            }
        } catch (e: Exception) {
            Log.e("getClientDevices", "Error fetching device information", e)
            null
        }
    }


        }
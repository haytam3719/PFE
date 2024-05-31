package com.example.project.repositories

import android.util.Log
import com.example.project.models.Client
import com.example.project.models.DeviceInfo
import com.example.project.prototype.ClientRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

class ClientRepositoryImpl : ClientRepository {
    private val databaseReference = FirebaseDatabase.getInstance().getReference("clients")
    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }
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


    override fun addDeviceToClient(clientUid: String, newDevice: DeviceInfo, completion: (Boolean) -> Unit) {
        databaseReference.orderByChild("Client/uid").equalTo(clientUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val clientEntry = snapshot.children.firstOrNull()
                    val clientKey = clientEntry?.key

                    if (clientKey != null) {
                        val clientDevicesPath = databaseReference.child(clientKey).child("Client").child("deviceInfoList")
                        clientDevicesPath.push().setValue(newDevice)
                            .addOnSuccessListener {
                                Log.d("ClientRepository", "Device added successfully to client with key: $clientKey")
                                completion(true)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("ClientRepository", "Failed to add device: ${exception.message}")
                                completion(false)
                            }
                    } else {
                        Log.e("ClientRepository", "Client key not found")
                        completion(false)
                    }
                } else {
                    Log.e("ClientRepository", "No client found with UID: $clientUid")
                    completion(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ClientRepository", "Error fetching client with UID: $clientUid; ${error.message}")
                completion(false)
            }
        })
    }


    fun getClientDetailsByUid(uid: String, callback: (Result<Client>) -> Unit) {
        val query: Query = databaseReference.orderByChild("Client/uid").equalTo(uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var found = false
                for (autoGenKeySnapshot in snapshot.children) {
                    val clientSnapshot = autoGenKeySnapshot.child("Client")
                    if (clientSnapshot.child("uid").value.toString() == uid) {
                        val client = clientSnapshot.getValue(Client::class.java)
                        if (client != null) {
                            callback(Result.success(client))
                            found = true
                            break
                        }
                    }
                }
                if (!found) {
                    callback(Result.failure(Exception("Client not found")))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(Result.failure(databaseError.toException()))
            }
        })
    }


    suspend fun fetchResource(resourcePath: String): Result<ByteArray> {
        return try {
            val resourceReference = storageReference.child(resourcePath)
            val data = resourceReference.getBytes(Long.MAX_VALUE).await()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}



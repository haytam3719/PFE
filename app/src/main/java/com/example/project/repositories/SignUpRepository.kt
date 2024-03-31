package com.example.project.repositories

import android.util.Log
import com.example.project.models.Client
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class SignUpRepository @Inject constructor() {
    private val database= Firebase.database.reference

    fun getStockClientById(clientId: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("users")
        ref.child(clientId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val client = snapshot.getValue(Client::class.java)
                if (client != null) {
                    Log.d("SignUpRepository", "Client retrieved successfully: $client")
                    // Process the retrieved client data further if needed
                } else {
                    Log.e("SignUpRepository", "Client doesn't exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("SignUpRepository", "onCancelled/SignUpRepo: ${databaseError.message}")
            }
        })
    }



    fun storeClient(client: Client) {
        val clientData = mapOf(
            "uid" to client.uid,
            "Client" to client
        )
        database.child("clients").push().setValue(clientData)
            .addOnSuccessListener {
                Log.d("SignUpRepository", "Client stored successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("SignUpRepository", "Failed to store client: ${exception.message}")
            }
    }

}
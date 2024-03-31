package com.example.project.repositories

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class AuthRepository @Inject constructor() {
     val auth = Firebase.auth
     private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun signUp(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun signIn(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun signOut() {
        auth.signOut()
    }


     fun signInWithUidAndPassword(uid: String, password: String) {
        // Retrieve the password from the database for the provided UID
        val userRef = database.reference.child("clients").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val storedPassword = dataSnapshot.getValue(String::class.java)

                if (storedPassword == password) {
                    // Sign in the user if passwords match
                    // You can implement sign-in logic here
                } else {
                    throw Exception("Invalid password")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                throw Exception("Database error: ${databaseError.message}")
            }
        })
    }

    fun getClientFieldById(userId: String, fieldName: String): String? {
        var fieldValue: String? = null
        val latch = CountDownLatch(1) // For synchronization

        val clientsRef: DatabaseReference = database.reference.child("clients").child(userId)

        clientsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val fieldValueSnapshot = dataSnapshot.child(fieldName)
                    fieldValue = fieldValueSnapshot.value as? String
                    Log.d("Firebase", "Field value for $fieldName: $fieldValue")
                } else {
                    Log.e("Firebase", "No client found with ID: $userId")
                }
                latch.countDown() // Release the latch
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${databaseError.message}")
                latch.countDown() // Release the latch
            }
        })

        Log.d("Firebase", "Querying client with ID: $userId")

        try {
            // Wait for the latch to count down to 0
            latch.await()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Log.e("Firebase", "Thread interrupted while waiting for latch")
        }

        return fieldValue
    }



    fun getClientById(userId: String) {
        val clientsRef: DatabaseReference = database.reference.child("clients").child(userId)

        clientsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Convert dataSnapshot to a Map<String, Any> for logging
                    val clientMap: Map<String, Any?> = dataSnapshot.value as? Map<String, Any?> ?: mapOf()

                    // Log the entire client object
                    Log.d("ClientObject", "Client object for userId $userId: $clientMap")

                    // Optionally, you can access specific fields from the client object
                    val firstName = clientMap["prenom"] as? String
                    val lastName = clientMap["nom"] as? String
                    // Log specific fields if needed
                    Log.d("ClientObject", "First Name: $firstName, Last Name: $lastName")
                } else {
                    Log.e("Failure", "No client found with ID: $userId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Error fetching data: ${databaseError.message}")
            }
        })
    }




}

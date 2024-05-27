package com.example.project.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

object FCMRepository {

    private val database = FirebaseDatabase.getInstance().reference

    fun saveOrUpdateFcmToken(token: String, onComplete: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("users").child(userId).child("fcmToken").setValue(token)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                    if (task.isSuccessful) {
                        Log.d("FCMRepo", "FCM token saved/updated successfully")
                    } else {
                        Log.w("FCMRepo", "Failed to save/update FCM token", task.exception)
                    }
                }
        } else {
            onComplete(false)
            Log.w("FCMRepo", "No current user logged in, cannot save/update FCM token")
        }
    }

    fun getFcmTokenForUser(userId: String, onComplete: (String?) -> Unit) {
        database.child("users").child(userId).child("fcmToken").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = snapshot.getValue(String::class.java)
                onComplete(token)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FCMRepo", "Failed to retrieve FCM token", error.toException())
                onComplete(null)
            }
        })
    }

    fun initializeAndSaveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCMRepo", "Token retrieved: $token")
                if (token != null) {
                    saveOrUpdateFcmToken(token) { success ->
                        if (success) {
                            Log.d("FCMRepo", "FCM token successfully saved/updated")
                        } else {
                            Log.w("FCMRepo", "Failed to save/update FCM token")
                        }
                    }
                }
            } else {
                Log.w("FCMRepo", "Fetching FCM registration token failed", task.exception)
            }
        }
    }
}

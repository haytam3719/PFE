package com.example.project.repositories

import android.util.Log
import com.example.project.prototype.CredentialsRepository
import com.google.firebase.database.FirebaseDatabase

class CredentialsRepositoryImpl(
    override val id_client: String,
    override val code_attijariSecure: String
) : CredentialsRepository{
    private val databaseReference = FirebaseDatabase.getInstance().getReference("credentials")

    override suspend fun storeAttijariSecureCode(uid: String, code: String) {
        databaseReference.child(uid).setValue(code)
            .addOnSuccessListener {
                println("Code successfully stored for user $uid")
            }
            .addOnFailureListener { e ->
                println("Failed to store code for user $uid: ${e.message}")
            }
    }

    override fun getAttijariSecureCodeForConnClient(uid: String, callback: (String?) -> Unit) {
        FirebaseDatabase.getInstance().getReference("credentials").child(uid)
            .get().addOnSuccessListener { dataSnapshot ->
                val code = dataSnapshot.value as String?
                callback(code)
            }.addOnFailureListener {
                Log.e("Credentials Repo", "Error fetching secure code", it)
                callback(null)
            }
    }


}
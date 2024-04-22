package com.example.project.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.project.models.Bill
import com.example.project.models.CompteImpl
import com.example.project.models.Paiement
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse
import com.example.project.prototype.PaymentApiService
import com.example.project.prototype.PaymentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val apiService: PaymentApiService, private val paiement: Paiement
) : PaymentRepository {
    override suspend fun getBills(): List<Bill> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getBills()
                if(response.isNotEmpty()){
                    response
                } else {
                    throw Exception("Empty response")
                }
            }
        } catch (e: Exception) {
            Log.e("REPO", "Error fetching bills", e)
            emptyList()
        }
    }



    override suspend fun requestPayment(paymentRequest: PaymentRequest): Result<PaymentResponse> {
        val response = apiService.requestPayment(paymentRequest)
        return if (response.isSuccess) {
            Result.success(response.getOrThrow())
        } else {
            Result.failure(Exception("Failed to make payment"))
        }
    }





    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun makePaiement(amount:Double, motif: String){
        fun getCurrentUserCourantAccount(callback: (CompteImpl?) -> Unit) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val uid = currentUser.uid
                val database = FirebaseDatabase.getInstance()
                val accountsRef = database.getReference("accounts")
                val query = accountsRef.orderByChild("id_proprietaire").equalTo(uid)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (accountSnapshot in dataSnapshot.children) {
                            val account = accountSnapshot.getValue(CompteImpl::class.java)
                            if (account != null && account.type.toString() == "COURANT") {
                                callback(account)
                                return
                            }
                        }
                        callback(null)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        println("Error fetching user's Courant account: ${databaseError.message}")
                        callback(null)
                    }
                })
            } else {
                callback(null)
            }
        }

        fun getBanqueAccount(callback: (CompteImpl?) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val accountsRef = database.getReference("accounts")

            val query = accountsRef.orderByChild("id_proprietaire").equalTo("Banque")

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (accountSnapshot in dataSnapshot.children) {
                            val account = accountSnapshot.getValue(CompteImpl::class.java)
                            if (account != null) {
                                callback(account)
                                return
                            }
                        }
                    }
                    callback(null)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error fetching Banque account: ${databaseError.message}")
                    callback(null)
                }
            })
        }


        getCurrentUserCourantAccount { currentUserCourantAccount ->
            if (currentUserCourantAccount != null) {
                getBanqueAccount { banqueAccount ->
                    if (banqueAccount != null) {
                        paiement.makePayment(amount, currentUserCourantAccount, banqueAccount, motif)
                    } else {
                //Banque account not found
                    }
                }
            } else {
                //Compte Courant wasn't found
            }
        }


    }


}

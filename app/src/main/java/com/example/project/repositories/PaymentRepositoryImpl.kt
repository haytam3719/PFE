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
    override suspend fun makePaiement(amount: Double, motif: String, selectedAccountId: String) {
        fun getUserSelectedAccount(callback: (CompteImpl?) -> Unit) {
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
                            if (account != null && account.numero == selectedAccountId) {
                                Log.d("makePaiement", "User selected account found: ${account.numero}")
                                callback(account)
                                return
                            }
                        }
                        Log.d("makePaiement", "User selected account not found")
                        callback(null)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("makePaiement", "Error fetching user's selected account: ${databaseError.message}")
                        callback(null)
                    }
                })
            } else {
                Log.d("makePaiement", "Current user is null")
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
                                Log.d("makePaiement", "Banque account found: ${account.numero}")
                                callback(account)
                                return
                            }
                        }
                    }
                    Log.d("makePaiement", "Banque account not found")
                    callback(null)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("makePaiement", "Error fetching Banque account: ${databaseError.message}")
                    callback(null)
                }
            })
        }

        getUserSelectedAccount { userSelectedAccount ->
            if (userSelectedAccount != null) {
                getBanqueAccount { banqueAccount ->
                    if (banqueAccount != null) {
                        Log.d("makePaiement", "Making payment from ${userSelectedAccount.numero} to ${banqueAccount.numero}")
                        try {
                            paiement.makePayment(amount, userSelectedAccount, banqueAccount, motif)
                            Log.d("makePaiement", "makePayment method was called")
                        } catch (e: Exception) {
                            Log.e("makePaiement", "Exception during makePayment: ${e.message}")
                        }
                    } else {
                        Log.e("makePaiement", "Banque account not found, cannot proceed with payment")
                    }
                }
            } else {
                Log.e("makePaiement", "User selected account not found, cannot proceed with payment")
            }
        }
    }





}

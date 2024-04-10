package com.example.project.repositories

import android.util.Log
import com.example.project.models.AccountCreationServiceImpl
import com.example.project.models.CompteImpl
import com.example.project.prototype.AccountRepository
import com.example.project.prototype.Compte
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(private val accountService:AccountCreationServiceImpl): AccountRepository {
    private val database = FirebaseDatabase.getInstance()
    override suspend fun getAccounts(): List<Compte> {
        val accountsRef = database.reference.child("Accounts")
        val dataSnapshot = accountsRef.get().await()

        val accounts = mutableListOf<Compte>()
        for (childSnapshot in dataSnapshot.children) {
            val account = childSnapshot.getValue(Compte::class.java)
            if (account != null) {
                accounts.add(account)
            }
        }
        return accounts
    }

    override suspend fun createAccount(account: Compte) {
        accountService.createAccount(
            account.numero,
            account.id_proprietaire,
            account.type,
            account.solde
        )
        val accountsRef = database.reference.child("accounts")
        val accountId = accountsRef.push().key ?: throw RuntimeException("Failed to create account")

        val accountData = account.toMap()
        accountsRef.child(accountId).setValue(accountData).await()
    }

    override suspend fun deleteAccount(accountId: String) {
        val accountRef = database.reference.child("Accounts").child(accountId)
        accountRef.removeValue().await()
    }

    override fun updateAccount(accountId: String, updatedAccount: Compte) {
        val accountsRef = database.reference.child("accounts")

        // Query to find the account with the specified ID
        val query: Query = accountsRef.orderByChild("numero").equalTo(accountId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        // Retrieve the key (Firebase push ID) of the existing account object
                        val accountKey = snapshot.key

                        if (accountKey != null) {
                            // Update the existing account node with the new data
                            accountsRef.child(accountKey).setValue(updatedAccount)
                                .addOnSuccessListener {
                                    Log.d("Update Account", "Account $accountId updated successfully")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Update Account ERROR", "Error updating account $accountId", exception)
                                }
                        }
                    }
                } else {
                    Log.e("Update Account", "Account with ID $accountId not found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Update Account ERROR", "Database error: ${databaseError.message}")
            }
        })
    }




    override fun getAccountByNumero(numero: String, callback: (CompteImpl?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        val query: Query = accountsRef.orderByChild("numero").equalTo(numero)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val account = snapshot.getValue(CompteImpl::class.java)
                        callback(account)
                        return
                    }
                } else {
                    // No matching account found
                    callback(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Firebase Database Error: ${databaseError.message}")
                callback(null)
            }
        })
    }
}

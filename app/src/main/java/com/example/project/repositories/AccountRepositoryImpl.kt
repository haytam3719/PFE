package com.example.project.repositories

import android.util.Log
import com.example.project.models.AccountCreationServiceImpl
import com.example.project.models.CompteImpl
import com.example.project.models.TransactionImpl
import com.example.project.prototype.AccountRepository
import com.example.project.prototype.Compte
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(private val accountService:AccountCreationServiceImpl): AccountRepository {
    private val database = FirebaseDatabase.getInstance("https://bank-2fd65-default-rtdb.firebaseio.com/")
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

    override suspend fun deleteAccountByNumero(numero: String) {
        val accountsRef = database.reference.child("accounts")
        val query = accountsRef.orderByChild("numero").equalTo(numero)
        val result = query.get().await()

        for (snapshot in result.children) {
            snapshot.ref.removeValue().await()
        }
    }

    override fun updateAccount(numero: String, updatedAccount: Compte) {
        val accountsRef = database.reference.child("accounts")

        val query = accountsRef.orderByChild("numero").equalTo(numero)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (accountSnapshot in snapshot.children) {
                        val key = accountSnapshot.key ?: return

                        accountsRef.child(key).setValue(updatedAccount)
                            .addOnSuccessListener {
                                Log.d("Update Account", "Account with numero $numero updated successfully.")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Update Account ERROR", "Error updating account $numero", exception)
                            }
                    }
                } else {
                    Log.e("Update Account ERROR", "No account found with numero $numero")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Update Account ERROR", "Database error when querying for numero $numero", databaseError.toException())
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




    suspend fun getAccountsForCurrentUser(userId: String): List<CompteImpl> {
        Log.d("AccountRepo", "Attempting to fetch accounts for user ID: $userId")
        return try {
            val dataSnapshot = database.reference.child("accounts")
                .orderByChild("id_proprietaire").equalTo(userId).get().await()

            Log.d("AccountRepo", "Query successful, dataSnapshot: $dataSnapshot")
            dataSnapshot.children.mapNotNull { it.getValue<CompteImpl>() }
        } catch (e: Exception) {
            Log.e("AccountRepo", "Error fetching user-specific accounts", e)
            emptyList()
        }
    }


    fun fetchHistoriqueTransactions(idProprietaire: String, callback: (List<TransactionImpl>?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val accountsRef = database.getReference("accounts")

        // Query to find all accounts where 'id_proprietaire' matches 'idProprietaire'
        val query = accountsRef.orderByChild("id_proprietaire").equalTo(idProprietaire)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val transactions = mutableListOf<TransactionImpl>()
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach { snapshot ->
                        val transactionsSnapshot = snapshot.child("historiqueTransactions")
                        transactionsSnapshot.children.forEach { transactionSnapshot ->
                            transactionSnapshot.getValue(TransactionImpl::class.java)?.let {
                                transactions.add(it)
                            }
                        }
                    }
                    callback(transactions)
                } else {
                    callback(emptyList())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        })
    }



    suspend fun fetchTransactionsByAccountNumber(accountNumber: String): Pair<List<TransactionImpl>?, String?> {
        val accountsRef = database.getReference("accounts")

        try {
            val dataSnapshot = accountsRef.orderByChild("numero").equalTo(accountNumber).get().await()
            if (dataSnapshot.exists()) {
                val transactions = mutableListOf<TransactionImpl>()
                dataSnapshot.children.forEach { accountSnapshot ->
                    val transactionsSnapshot = accountSnapshot.child("historiqueTransactions")
                    transactionsSnapshot.children.mapNotNullTo(transactions) {
                        it.getValue(TransactionImpl::class.java)
                    }
                }
                return Pair(transactions, null)
            } else {
                return Pair(null, "No account found with the specified account number: $accountNumber")
            }
        } catch (e: Exception) {
            return Pair(null, "Database error: ${e.message}")
        }
    }

    suspend fun getHistoriqueTransactionsById(transactionId: String): TransactionImpl? = withContext(Dispatchers.IO) {
        try {
            val accountsReference = FirebaseDatabase.getInstance().getReference("accounts")
            val snapshot = accountsReference.get().await()
            var transaction: TransactionImpl? = null

            for (accountSnapshot in snapshot.children) {
                val transactionsSnapshot = accountSnapshot.child("historiqueTransactions")
                transaction = transactionsSnapshot.children.firstOrNull {
                    it.child("idTran").getValue(String::class.java) == transactionId
                }?.getValue(TransactionImpl::class.java)

                if (transaction != null) {
                    Log.d("TransactionRepository", "Transaction fetched successfully: $transaction")
                    break
                }
            }

            if (transaction == null) {
                Log.d("TransactionRepository", "No transaction found with ID: $transactionId")
            }
            transaction
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching transactions: ", e)
            null
        }
    }


    fun getTotalBalanceByClientUid(clientUid: String, callback: (Double) -> Unit) {
        database.getReference("accounts").orderByChild("id_proprietaire").equalTo(clientUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var totalBalance = 0.0
                        snapshot.children.forEach {
                            val account = it.getValue(CompteImpl::class.java)
                            totalBalance += account?.solde ?: 0.0
                        }
                        callback(totalBalance)
                    } else {
                        callback(0.0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Firebase error: ${error.message}")
                    callback(0.0)
                }
            })

    }


    fun fetchAccountBalances(userId: String, callback: (List<Pair<String, Double>>, Boolean) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("accounts")
        databaseReference.orderByChild("id_proprietaire").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balances = mutableListOf<Pair<String, Double>>()
                snapshot.children.forEach { accountSnapshot ->
                    val account = accountSnapshot.getValue(CompteImpl::class.java)
                    account?.let {
                        balances.add(Pair(it.type.toString(), it.solde))
                    }
                }
                callback(balances, true)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
                callback(emptyList(), false)
            }
        })
    }


    fun fetchAccountBalancesOverTime(userId: String, callback: (List<Pair<String, Double>>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("accounts")
        dbRef.orderByChild("id_proprietaire").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balanceChanges = mutableListOf<Pair<String, Double>>()
                snapshot.children.forEach { accountSnapshot ->
                    val account = accountSnapshot.getValue(CompteImpl::class.java)
                    account?.historiqueTransactions?.forEach { transaction ->
                        val date = transaction.date
                        val amount = transaction.montant
                        val isOutgoing = transaction.compteEmet.id_proprietaire == userId
                        val isIncoming = transaction.compteBenef.id_proprietaire == userId

                        // Determine balance change based on the direction of the transaction
                        val currentBalanceChange = when {
                            isIncoming -> amount
                            isOutgoing -> -amount
                            else -> 0.0
                        }

                        val existingEntry = balanceChanges.find { it.first == date }
                        if (existingEntry != null) {
                            val index = balanceChanges.indexOf(existingEntry)
                            balanceChanges[index] = existingEntry.copy(second = existingEntry.second + currentBalanceChange)
                        } else {
                            balanceChanges.add(Pair(date, currentBalanceChange))
                        }
                    }
                }
                val sortedBalances = balanceChanges.sortedBy { it.first }
                var cumulativeBalance = 0.0
                val finalBalances = sortedBalances.map { balanceChange ->
                    cumulativeBalance += balanceChange.second
                    Pair(balanceChange.first, cumulativeBalance)
                }
                callback(finalBalances)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
}

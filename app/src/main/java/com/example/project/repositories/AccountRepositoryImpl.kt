package com.example.project.repositories

import android.util.Log
import com.example.project.models.AccountCreationServiceImpl
import com.example.project.models.Client
import com.example.project.models.ClientAccountDetails
import com.example.project.models.CompteImpl
import com.example.project.models.DeviceInfo
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
    private val database =
        FirebaseDatabase.getInstance("https://bank-2fd65-default-rtdb.firebaseio.com/")

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
                                Log.d(
                                    "Update Account",
                                    "Account with numero $numero updated successfully."
                                )
                            }
                            .addOnFailureListener { exception ->
                                Log.e(
                                    "Update Account ERROR",
                                    "Error updating account $numero",
                                    exception
                                )
                            }
                    }
                } else {
                    Log.e("Update Account ERROR", "No account found with numero $numero")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "Update Account ERROR",
                    "Database error when querying for numero $numero",
                    databaseError.toException()
                )
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


    fun fetchHistoriqueTransactions(
        idProprietaire: String,
        callback: (List<TransactionImpl>?) -> Unit
    ) {
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
            val dataSnapshot =
                accountsRef.orderByChild("numero").equalTo(accountNumber).get().await()
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
                return Pair(
                    null,
                    "No account found with the specified account number: $accountNumber"
                )
            }
        } catch (e: Exception) {
            return Pair(null, "Database error: ${e.message}")
        }
    }

    suspend fun getHistoriqueTransactionsById(transactionId: String): TransactionImpl? =
        withContext(Dispatchers.IO) {
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
                        Log.d(
                            "TransactionRepository",
                            "Transaction fetched successfully: $transaction"
                        )
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


    fun fetchAccountBalances(
        userId: String,
        callback: (List<Pair<String, Double>>, Boolean) -> Unit
    ) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("accounts")
        databaseReference.orderByChild("id_proprietaire").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
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


    fun fetchAccountBalancesOverTime(
        userId: String,
        callback: (List<Pair<String, Double>>) -> Unit
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("accounts")
        dbRef.orderByChild("id_proprietaire").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                                balanceChanges[index] =
                                    existingEntry.copy(second = existingEntry.second + currentBalanceChange)
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


    fun getBeneficiariesForUser(userId: String, callback: (Result<List<CompteImpl>>) -> Unit) {
        database.getReference("accounts")
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val beneficiaries = mutableListOf<CompteImpl>()
                    task.result?.children?.forEach { accountSnapshot ->
                        Log.d("Repository", "Account Snapshot: ${accountSnapshot.key} -> ${accountSnapshot.value}")
                        accountSnapshot.child("historiqueTransactions").children.forEach { transactionSnapshot ->
                            Log.d("Repository", "Transaction Snapshot: ${transactionSnapshot.key} -> ${transactionSnapshot.value}")
                            val compteEmetId = transactionSnapshot.child("compteEmet/id_proprietaire").getValue(String::class.java)
                            if (compteEmetId == userId) {
                                val compteBenef = transactionSnapshot.child("compteBenef").getValue(CompteImpl::class.java)
                                Log.d("Repository", "CompteBenef: ${compteBenef}")
                                compteBenef?.let {
                                    // Check if this compteBenef is already in the list to avoid duplicates
                                    if (!beneficiaries.any { b -> b.numero == it.numero }) {
                                        beneficiaries.add(it)
                                    }
                                }
                            }
                        }
                    }
                    Log.d("Repository", "Beneficiaries fetched: ${beneficiaries.size}")
                    callback(Result.success(beneficiaries))
                } else {
                    task.exception?.let {
                        Log.e("Repository", "Error fetching beneficiaries: ${it.message}")
                        callback(Result.failure(it))
                    }
                }
            }
    }

    fun getClientDetailsByUid(uid: String, callback: (Result<Client>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("clients")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (autoGenKeySnapshot in snapshot.children) {
                    val clientSnapshot = autoGenKeySnapshot.child("Client")
                    if (clientSnapshot.child("uid").value.toString() == uid) {
                        try {
                            val client = Client(
                                uid = clientSnapshot.child("uid").getValue(String::class.java),
                                nom = clientSnapshot.child("nom").getValue(String::class.java) ?: "",
                                prenom = clientSnapshot.child("prenom").getValue(String::class.java) ?: "",
                                date_naissanace = clientSnapshot.child("date_naissanace").getValue(String::class.java) ?: "",
                                adresse = clientSnapshot.child("adresse").getValue(String::class.java) ?: "",
                                numCin = clientSnapshot.child("numCin").getValue(String::class.java) ?: "",
                                domicile = clientSnapshot.child("domicile").getValue(String::class.java) ?: "",
                                numTele = clientSnapshot.child("numTele").getValue(String::class.java) ?: "",
                                fingerPrint = clientSnapshot.child("fingerPrint").getValue(String::class.java) ?: "",
                                identityCardFrontUrl = clientSnapshot.child("identityCardFrontUrl").getValue(String::class.java),
                                identityCardBackUrl = clientSnapshot.child("identityCardBackUrl").getValue(String::class.java),
                                facePhotoUrl = clientSnapshot.child("facePhotoUrl").getValue(String::class.java),
                                deviceInfo = null,
                                deviceInfoList = convertDeviceInfoList(clientSnapshot.child("deviceInfoList"))
                            )
                            callback(Result.success(client))
                            break
                        } catch (e: Exception) {
                            Log.e("getClientDetails", "Error processing data for client UID: $uid, ${e.message}")
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(Result.failure(databaseError.toException()))
            }
        })
    }

    private fun convertDeviceInfoList(deviceInfoSnapshot: DataSnapshot): List<DeviceInfo>? {
        val devices = mutableListOf<DeviceInfo>()
        deviceInfoSnapshot.children.forEach { deviceSnapshot ->
            deviceSnapshot.getValue(DeviceInfo::class.java)?.let {
                devices.add(it)
            }
        }
        return if (devices.isEmpty()) null else devices
    }




    fun getCombinedBeneficiaryClientData(userId: String, callback: (Result<List<ClientAccountDetails>>) -> Unit) {
        getBeneficiariesForUser(userId) { result ->
            result.onSuccess { beneficiaries ->
                val combinedData = mutableListOf<ClientAccountDetails>()
                Log.d("Repository", "Beneficiaries fetched: ${beneficiaries.size}")

                if (beneficiaries.isEmpty()) {
                    Log.d("Repository", "No beneficiaries found, returning empty list")
                    callback(Result.success(combinedData))
                    return@onSuccess
                }

                for (beneficiary in beneficiaries) {
                    Log.d("Repository", "Fetching client details for id_proprietaire: ${beneficiary.id_proprietaire}")
                    getClientDetailsByUid(beneficiary.id_proprietaire) { clientResult ->
                        clientResult.onSuccess { client ->
                            Log.d("Repository", "Client details fetched: $client")
                            combinedData.add(
                                ClientAccountDetails(
                                    nom = client.nom,
                                    prenom = client.prenom,
                                    profileImageUrl = null,
                                    accountNumber = beneficiary.numero,
                                )
                            )
                        }.onFailure { exception ->
                            Log.e("Repository", "Error fetching client details for id_proprietaire: ${beneficiary.id_proprietaire}, error: ${exception.message}")
                        }
                    }
                }

                callback(Result.success(combinedData))
            }.onFailure { exception ->
                Log.e("Repository", "Error fetching beneficiaries: ${exception.message}")
                callback(Result.failure(exception))
            }
        }
    }





    suspend fun fetchTransactionsByPaymentMethod(accountNumber: String): Pair<List<TransactionImpl>?, String?> {
        val accountsRef = database.getReference("accounts")

        try {
            Log.d("fetchTransactions", "Fetching transactions for account number: $accountNumber")

            val dataSnapshot =
                accountsRef.orderByChild("numero").equalTo(accountNumber).get().await()
            if (dataSnapshot.exists()) {
                Log.d("fetchTransactions", "Account found for account number: $accountNumber")
                val transactions = mutableListOf<TransactionImpl>()
                dataSnapshot.children.forEach { accountSnapshot ->
                    val transactionsSnapshot = accountSnapshot.child("historiqueTransactions")
                    transactionsSnapshot.children.forEach { transactionSnapshot ->
                        val transaction = transactionSnapshot.getValue(TransactionImpl::class.java)
                        if (transaction?.methodPaiement?.startsWith("Carte Débit") == true) {
                            transactions.add(transaction)
                            Log.d("fetchTransactions", "Transaction added: $transaction")
                        }
                    }
                }
                Log.d("fetchTransactions", "Total transactions fetched: ${transactions.size}")
                return Pair(transactions, null)
            } else {
                Log.d("fetchTransactions", "No account found with the specified account number: $accountNumber")
                return Pair(
                    null,
                    "No account found with the specified account number: $accountNumber"
                )
            }
        } catch (e: Exception) {
            Log.e("fetchTransactions", "Database error: ${e.message}", e)
            return Pair(null, "Database error: ${e.message}")
        }
    }




}

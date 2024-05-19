package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.adapters.AccountData
import com.example.project.models.ClientAccountDetails
import com.example.project.models.CompteImpl
import com.example.project.prototype.Compte
import com.example.project.prototype.Transaction
import com.example.project.repositories.AccountRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ConsultationViewModel @Inject constructor(private val accountRepositoryImpl: AccountRepositoryImpl) : ViewModel() {
    private val _accounts = MutableLiveData<List<AccountData>>()
    val accounts: LiveData<List<AccountData>> get() = _accounts

    init {
        getAccounts()
    }

    fun getAccounts() {
        viewModelScope.launch {
            accountRepositoryImpl.getAccounts()
        }
    }

    fun createAccount(account: Compte) {
        viewModelScope.launch {
            accountRepositoryImpl.createAccount(account)
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            accountRepositoryImpl.deleteAccountByNumero(accountId)
        }
    }

    fun updateAccount(accountId: String, updatedAccount: Compte) {
        viewModelScope.launch {
            accountRepositoryImpl.updateAccount(accountId, updatedAccount)
        }
    }

    fun getAccountByNumero(numero: String, callback: (CompteImpl?) -> Unit) {
        viewModelScope.launch {
            accountRepositoryImpl.getAccountByNumero(numero, callback)
        }
    }


    val userAccountsLiveData = MutableLiveData<List<CompteImpl>>()

    fun fetchAccountsForCurrentUser(userId: String) {
        viewModelScope.launch {
            val accounts = accountRepositoryImpl.getAccountsForCurrentUser(userId)
            _accounts.postValue(accounts.map { AccountData(it.type.toString(), it.numero, it.solde.toString()) })
        }
    }



    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun loadTransactions(uid: String) {
        viewModelScope.launch {
            val accountId = FirebaseAuth.getInstance().currentUser?.uid
            if (accountId != null) {
                accountRepositoryImpl.fetchHistoriqueTransactions(accountId) { transactions ->
                    transactions?.let {
                        _transactions.postValue(it)
                    } ?: run {
                        Log.e("ViewModel", "No transactions found.")
                        _transactions.postValue(emptyList())
                    }
                }
            } else {
                Log.e("ViewModel", "Account ID is null")
                _transactions.postValue(emptyList())
            }
        }
    }


    private val _selectedAccount = MutableLiveData<AccountData>()
    val selectedAccount: LiveData<AccountData> = _selectedAccount
    fun selectAccount(account: AccountData) {
        _selectedAccount.value = account
    }

    val combinedDataLiveData = MutableLiveData<Result<List<ClientAccountDetails>>>()

    fun loadCombinedData(userId: String) {
        Log.d("ViewModel", "Starting loadCombinedData for userId: $userId")
        viewModelScope.launch {
            accountRepositoryImpl.getCombinedBeneficiaryClientData(userId) { result ->
                result.onSuccess { combinedData ->
                    Log.d("ViewModel", "Combined data loaded successfully: $combinedData")
                }
                result.onFailure { exception ->
                    Log.e("ViewModel", "Error loading combined data: ${exception.message}")
                }
                combinedDataLiveData.postValue(result)
            }
        }
    }


    private val _selectedClient = MutableLiveData<ClientAccountDetails>()
    val selectedClient: LiveData<ClientAccountDetails> = _selectedClient

    fun selectClient(client: ClientAccountDetails) {
        _selectedClient.value = client
    }

}

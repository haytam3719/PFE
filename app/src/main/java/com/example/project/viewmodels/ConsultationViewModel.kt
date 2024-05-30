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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _dataLoaded = MutableLiveData<Boolean>()
    val dataLoaded: LiveData<Boolean> get() = _dataLoaded

    private val _accounts = MutableLiveData<List<AccountData>>()
    val accounts: LiveData<List<AccountData>> get() = _accounts

    init {
        getAccounts()
    }

    fun getAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accounts = accountRepositoryImpl.getAccounts()
                _dataLoaded.postValue(true)
            } catch (e: Exception) {
                Log.e("ConsultationViewModel", "Error fetching accounts", e)
                _dataLoaded.postValue(false)
            } finally {
                _isLoading.value = false
            }
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
            _isLoading.value = true
            try {
                val accounts = accountRepositoryImpl.getAccountsForCurrentUser(userId)
                _accounts.postValue(accounts.map { AccountData(it.type.toString(), it.numero, it.solde.toString()) })
                _dataLoaded.postValue(true)
            } catch (e: Exception) {
                Log.e("ConsultationViewModel", "Error fetching accounts for current user", e)
                _dataLoaded.postValue(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    fun loadTransactions(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accountId = FirebaseAuth.getInstance().currentUser?.uid
                if (accountId != null) {
                    accountRepositoryImpl.fetchHistoriqueTransactions(accountId) { transactions ->
                        transactions?.let {
                            _transactions.postValue(it)
                        } ?: run {
                            Log.e("ConsultationViewModel", "No transactions found.")
                            _transactions.postValue(emptyList())
                        }
                        _dataLoaded.postValue(true)
                    }
                } else {
                    Log.e("ConsultationViewModel", "Account ID is null")
                    _transactions.postValue(emptyList())
                    _dataLoaded.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("ConsultationViewModel", "Error loading transactions", e)
                _dataLoaded.postValue(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _selectedAccount = MutableLiveData<AccountData>()
    val selectedAccount: LiveData<AccountData> get() = _selectedAccount

    fun selectAccount(account: AccountData) {
        _selectedAccount.value = account
    }

    val combinedDataLiveData = MutableLiveData<Result<List<ClientAccountDetails>>>()

    fun loadCombinedData(userId: String) {
        Log.d("ConsultationViewModel", "Starting loadCombinedData for userId: $userId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                accountRepositoryImpl.getCombinedBeneficiaryClientData(userId) { result ->
                    Log.d("ConsultationViewModel", "Received result from repository")
                    result.onSuccess { combinedData ->
                        Log.d("ConsultationViewModel", "Combined data loaded successfully: ${combinedData.size}")
                        combinedDataLiveData.postValue(Result.success(combinedData))
                        _dataLoaded.postValue(true)
                    }
                    result.onFailure { exception ->
                        Log.e("ConsultationViewModel", "Error loading combined data: ${exception.message}")
                        combinedDataLiveData.postValue(Result.failure(exception))
                        _dataLoaded.postValue(false)
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _selectedClient = MutableLiveData<ClientAccountDetails>()
    val selectedClient: LiveData<ClientAccountDetails> get() = _selectedClient

    fun selectClient(client: ClientAccountDetails) {
        _selectedClient.value = client
    }

    fun resetDataLoaded() {
        _dataLoaded.value = false
    }

}

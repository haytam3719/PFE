package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.prototype.Compte
import com.example.project.prototype.Transaction
import com.example.project.repositories.AccountRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class DetailCompteViewModel @Inject constructor(private val accountRepositoryImpl: AccountRepositoryImpl) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _transactionsForSingleAcc = MutableLiveData<List<Transaction>>()
    val transactionsForSingleAcc: LiveData<List<Transaction>> = _transactionsForSingleAcc
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadTransactionsForSingleAcc(accountNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (transactions, errorMessage) = accountRepositoryImpl.fetchTransactionsByAccountNumber(accountNumber)
                if (errorMessage == null) {
                    _transactionsForSingleAcc.postValue(transactions ?: emptyList())
                } else {
                    _error.postValue(errorMessage!!)
                    Log.e("DetailCompteViewModel", errorMessage)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }


    private val _deletionSuccess = MutableLiveData<Boolean>()
    val deletionSuccess: LiveData<Boolean> = _deletionSuccess

    private val _deletionError = MutableLiveData<String>()
    val deletionError: LiveData<String> = _deletionError

    fun deleteAccount(numeroAccount: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            try {
                accountRepositoryImpl.deleteAccountByNumero(numeroAccount)
                _deletionSuccess.postValue(true)
            } catch (e: Exception) {
                _deletionError.postValue(e.message ?: "Failed to delete account")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }



    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> = _updateStatus

    private val _updateError = MutableLiveData<String>()
    val updateError: LiveData<String> = _updateError

    fun updateAccount(numero: String, updatedAccount: Compte) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                accountRepositoryImpl.updateAccount(numero, updatedAccount)
                _updateStatus.postValue(true)
            } catch (e: Exception) {
                _updateError.postValue(e.message ?: "Error updating account.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
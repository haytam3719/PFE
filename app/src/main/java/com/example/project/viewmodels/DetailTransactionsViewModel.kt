package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.prototype.Transaction
import com.example.project.repositories.AccountRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class DetailTransactionsViewModel @Inject constructor(
    private val accountRepositoryImpl: AccountRepositoryImpl
) : ViewModel() {

    private val _transactionDetails = MutableLiveData<Transaction?>()
    val transactionDetails: LiveData<Transaction?> = _transactionDetails

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transaction = accountRepositoryImpl.getHistoriqueTransactionsById(transactionId)
                _transactionDetails.postValue(transaction)
            } catch (e: Exception) {
                _transactionDetails.postValue(null)
            }
        }
    }
}

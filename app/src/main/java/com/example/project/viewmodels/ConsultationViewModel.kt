package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.adapters.AccountData
import com.example.project.models.CompteImpl
import com.example.project.prototype.Compte
import com.example.project.repositories.AccountRepositoryImpl
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
            accountRepositoryImpl.deleteAccount(accountId)
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





}

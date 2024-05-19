package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.adapters.AccountData
import com.example.project.models.ClientAccountDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VirementUpdatedViewModel @Inject constructor() : ViewModel() {

    private val _selectedAccount = MutableLiveData<AccountData>()
    val selectedAccount: LiveData<AccountData> get() = _selectedAccount

    private val _selectedClient = MutableLiveData<ClientAccountDetails>()
    val selectedClient: LiveData<ClientAccountDetails> get() = _selectedClient

    private val _motif = MutableLiveData<String>()
    val motif: LiveData<String> get() = _motif

    private val _amount = MutableLiveData<Int>()
    val amount: LiveData<Int> get() = _amount

    private val _isTransferPlanned = MutableLiveData<Boolean>()
    val isTransferPlanned: LiveData<Boolean> get() = _isTransferPlanned

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> get() = _selectedDate

    fun selectAccount(account: AccountData) {
        _selectedAccount.value = account
    }

    fun selectClient(client: ClientAccountDetails) {
        _selectedClient.value = client
    }

    fun setMotif(motif: String) {
        _motif.value = motif
    }

    fun setAmount(amount: Int) {
        _amount.value = amount
    }

    fun setTransferPlanned(isPlanned: Boolean) {
        _isTransferPlanned.value = isPlanned
    }

    fun setSelectedDate(date: String?) {
        _selectedDate.value = date
    }
}

package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor() : ViewModel() {

    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> get() = _phoneNumber

    private val _montant = MutableLiveData<String>()
    val montant: LiveData<String> get() = _montant

    private val _rechargeType = MutableLiveData<String>()
    val rechargeType: LiveData<String> get() = _rechargeType

    fun setPhoneNumber(phone: String) {
        Log.d("RechargeViewModel", "Set Phone Number: $phone")
        _phoneNumber.value = phone
    }

    fun setMontant(amount: String) {
        Log.d("RechargeViewModel", "Set Montant: $amount")
        _montant.value = amount
    }

    fun setRechargeType(type: String) {
        Log.d("RechargeViewModel", "Set Recharge Type: $type")
        _rechargeType.value = type
    }
}

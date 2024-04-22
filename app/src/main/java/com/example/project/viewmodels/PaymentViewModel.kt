package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.Bill
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse
import com.example.project.prototype.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class PaymentViewModel @Inject constructor(private val paymentRepository: PaymentRepository): ViewModel(){

    private val _bills = MutableLiveData<List<Bill>>()
    val bills: LiveData<List<Bill>> = _bills

    private val _paymentResponse = MutableLiveData<PaymentResponse>()
    val paymentResponse: LiveData<PaymentResponse> = _paymentResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    fun getBills() {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "Fetching bills...")
            val result = withContext(Dispatchers.IO) {
                paymentRepository.getBills()
            }
            _bills.value = result
            Log.e("PaymentViewModel", "Bills fetched: $result")

            _error.value = "Error message"
            Log.e("Error", "Error message")

        }
    }

    fun requestPayment(paymentRequest: PaymentRequest) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                paymentRepository.requestPayment(paymentRequest)
            }
            result.onSuccess { _paymentResponse.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    fun makePaiement(amount:Double, motif: String){
        viewModelScope.launch {
            paymentRepository.makePaiement(amount,motif)
        }
    }

}
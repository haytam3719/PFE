package com.example.project.viewmodels

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.Client
import com.example.project.models.CompteImpl
import com.example.project.repositories.AccountRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class OtpViewModel @Inject constructor(private val accountRepositoryImpl: AccountRepositoryImpl):ViewModel(){

    private val _navigateToPrint = MutableLiveData<Boolean>()
    val navigateToPrint: LiveData<Boolean>
        get() = _navigateToPrint


    val _otpBiometricVerified = MutableLiveData<Boolean>()
    val otpBiometricVerified: LiveData<Boolean>
        get() = _otpBiometricVerified


    val _otpBiometricVerifiedPayment = MutableLiveData<Boolean>()
    val otpBiometricVerifiedPayment: LiveData<Boolean>
        get() = _otpBiometricVerifiedPayment



    fun onButtonClickProvisoire(context: Activity, enteredText: String, actualText: String, bundleValue:String) {
        viewModelScope.launch {
            Log.e("Coroutine", "Inside coroutine")
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Debug","debugProvisoireFunction")

                if (enteredText == actualText) {
                    Log.e("Debug","It entered")

                    when (bundleValue) {
                        "fromVirement" -> {
                            _otpBiometricVerified.value = true
                        }
                        "fromPayment" -> _otpBiometricVerifiedPayment.value = true
                        else -> _navigateToPrint.value = true
                    }
                    }

            } else {
                val PERMISSION_REQUEST_SEND_SMS = 123
                requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.SEND_SMS),
                    PERMISSION_REQUEST_SEND_SMS
                )
            }
        }


    }

    fun generateOTP(): String {
        val otpLength = 6
        val otp = StringBuilder()

        repeat(otpLength) {
            val digit = (0..9).random() // Generate a random digit (0-9)
            otp.append(digit)
        }

        return otp.toString()
    }
/*
    fun onButtonClick(view: View) {
        Log.e("Debug","debug")
        val context = view.context as? Activity ?: run {
            Log.e("Debug", "Context is not an Activity")
            return
        }
        val enteredText = (view.rootView.findViewById<EditText>(com.example.project.R.id.editTextOTP)).text.toString()

        Log.e("Debug", "Entered text: $enteredText")

        this.onButtonClickProvisoire(context, enteredText)
        Log.e("Debug",enteredText)
    }


*/
    // Function to reset navigation event after navigation
    fun onNavigationComplete() {
        _navigateToPrint.value = false
    }



    private val _clientDetails = MutableLiveData<Result<Client>>()
    val clientDetails: LiveData<Result<Client>> = _clientDetails

    private val _sendSmsTrigger = MutableLiveData<Pair<String, String>>()
    val sendSmsTrigger: LiveData<Pair<String, String>> = _sendSmsTrigger

    fun getClientDetailsByUid(uid: String) {
        Log.d("OtpViewModel", "Fetching client details for UID: $uid")
        accountRepositoryImpl.getClientDetailsByUid(uid) { result ->
            Log.d("OtpViewModel", "Received result for client details")
            _clientDetails.postValue(result)
            result.onSuccess { client ->
                Log.d("OtpViewModel", "Client details fetched successfully: $client")
                client.numTele?.let { phoneNumber ->
                    Log.d("OtpViewModel", "Triggering SMS send to phone number: $phoneNumber")
                    //_sendSmsTrigger.postValue(Pair(phoneNumber, "Votre code de vérification"))
                } ?: run {
                    Log.e("OtpViewModel", "Client phone number is null")
                }
            }
            result.onFailure { exception ->
                Log.e("OtpViewModel", "Failed to fetch client details: ${exception.message}", exception)
            }
        }
    }




    private val _recipientClientDetails = MutableLiveData<Result<Client>>()
    val recipientClientDetails: LiveData<Result<Client>> = _recipientClientDetails

    fun getRecipientClientDetailsByUid(uid: String) {
        Log.d("OtpViewModel", "Fetching recipient client details for UID: $uid")
        accountRepositoryImpl.getClientDetailsByUid(uid) { result ->
            Log.d("OtpViewModel", "Received result for recipient client details $result")
            _recipientClientDetails.postValue(result)
        }
    }


    suspend fun fetchRecipientClientDetails(uid: String): Client {
        return suspendCoroutine { continuation ->
            getRecipientClientDetailsByUid(uid)
            recipientClientDetails.observeForever(object : Observer<Result<Client>> {
                override fun onChanged(result: Result<Client>) {
                    result?.let {
                        if (it.isSuccess) {
                            continuation.resume(it.getOrNull()!!)
                        } else {
                            continuation.resumeWithException(it.exceptionOrNull()!!)
                        }
                    }
                    recipientClientDetails.removeObserver(this)
                }
            })
        }
    }


    private val _accountDetails = MutableLiveData<CompteImpl?>()
    val accountDetails: LiveData<CompteImpl?> get() = _accountDetails

    fun fetchAccountByNumero(numero: String) {
        accountRepositoryImpl.getAccountByNumero(numero) { account ->
            _accountDetails.postValue(account)
        }
    }

}


package com.example.project.viewmodels

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OtpViewModel @Inject constructor():ViewModel(){

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
                        "fromVirement" -> _otpBiometricVerified.value = true
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



}
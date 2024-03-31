package com.example.project.viewmodels

import android.app.Activity
import android.content.pm.PackageManager
import android.telephony.SmsManager
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


    fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            // SMS sent successfully
        } catch (e: Exception) {
            // Failed to send SMS
            e.printStackTrace()
        }
    }

    fun onButtonClickProvisoire(context: Activity, enteredText: String) {
        viewModelScope.launch {
            Log.e("Coroutine", "Inside coroutine")
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Debug","debugProvisoireFunction")
                sendSMS("1234567890", "Your OTP is: 123456")
                // Check if the entered text is "1234567890"
                if (enteredText == "1234567890") {
                    Log.e("Debug","It enterred")
                    _navigateToPrint.value = true // Trigger navigation
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
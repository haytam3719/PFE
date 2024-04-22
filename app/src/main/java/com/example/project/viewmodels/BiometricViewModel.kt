package com.example.project.viewmodels

import android.util.Base64
import android.util.Log
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.MessageDigest
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class BiometricViewModel @Inject constructor() : ViewModel() {

    private val _navigateToSacnCin = MutableLiveData<Boolean>()
    val navigateToScanCin: LiveData<Boolean>

        get() = _navigateToSacnCin


    private val _navigateToDashboard = MutableLiveData<Boolean>()
    val navigateToDashboard: LiveData<Boolean>
        get() = _navigateToDashboard


    private val _navigateToOtp = MutableLiveData<Boolean>()
    val navigateToOtp: LiveData<Boolean>
        get() = _navigateToOtp


    private val _biometricEvent = MutableLiveData<BiometricEvent>()
    val biometricEvent: LiveData<BiometricEvent>
        get() = _biometricEvent

    private var temp: ByteArray = ByteArray(0) // Initialize temp with an empty byte array

    private var registeredFingerprintHash: String? = null

    fun setGetRegisteredFingerprintData(fingerprintData: ByteArray): String? {
        registeredFingerprintHash = generateHash(fingerprintData)
        return registeredFingerprintHash
    }

    val _showBiometricPrompt = MutableLiveData<Boolean>()
    val showBiometricPrompt: LiveData<Boolean>
        get() = _showBiometricPrompt

    fun onButtonClick(view: View) {
        _showBiometricPrompt.value = true
    }

    fun getBiometricCallback(): BiometricPrompt.AuthenticationCallback {
        return biometricAuthenticationCallback
    }

    fun onBiometricPromptShown() {
        _showBiometricPrompt.value = false
    }

    private val biometricAuthenticationCallback =
        object : BiometricPrompt.AuthenticationCallback() {
            @Suppress("DEPRECATION")
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                _biometricEvent.postValue(BiometricEvent.Error(errorCode, errString.toString()))
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // Get the crypto object from the authentication result
                val cryptoObject = result.cryptoObject

                // Check if the crypto object is not null
                if (cryptoObject != null) {
                    // Use the cipher from the crypto object to encrypt your data
                    val cipher = cryptoObject.cipher
                    val fingerprintData: ByteArray? = result.cryptoObject?.cipher?.doFinal("FingerprintData".toByteArray())
                    val fingerprintDataString: String = fingerprintData?.let { Base64.encodeToString(it, Base64.DEFAULT) } ?: "No fingerprint data"
                    if (fingerprintData != null) {
                        temp=fingerprintData
                        Log.e("Finger Print",fingerprintDataString)
                        Log.e("Actual Hash",generateHash(fingerprintData))

                    }

                } else {
                    // Handle the case where the crypto object is null
                    Log.e("Biometric", "Crypto object is null")
                }

                _navigateToSacnCin.value = true
                _navigateToDashboard.value = true
                _navigateToOtp.value = true
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                _biometricEvent.postValue(BiometricEvent.Failure("Authentication failed"))
            }
        }


    fun getBiometricPrint(): ByteArray {
        return temp
    }

    // Function to reset navigation event after navigation
    fun onNavigationComplete() {
        _navigateToSacnCin.value = false
    }


    fun onNavigationCompleteDash(){
        _navigateToDashboard.value = false
    }


    fun onNavigationCompleteOtp(){
        _navigateToOtp.value = false
    }

    private fun generateHash(data: ByteArray): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }




    sealed class BiometricEvent {
        data class Success(val fingerprintData: ByteArray?) : BiometricEvent()
        data class Error(val errorCode: Int, val errorMessage: String) : BiometricEvent()
        data class Failure(val errorMessage: String) : BiometricEvent()
    }
}

class MainThreadExecutor @Inject constructor() : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }




    //-----------------------------------------------------------------------------------------------------------------


}

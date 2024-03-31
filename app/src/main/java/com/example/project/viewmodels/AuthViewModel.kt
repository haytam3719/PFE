package com.example.project.viewmodels

import android.util.Base64
import android.util.Log
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.project.models.AuthState
import com.example.project.models.LogInUserPartial
import com.example.project.repositories.AuthRepository
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var fingerPrint: String = ""
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    private lateinit var navController: NavController
    private val _navigateToCollectInfos = MutableLiveData<Boolean>()
    val navigateToCollectInfos: LiveData<Boolean>
        get() = _navigateToCollectInfos

    suspend fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.emit(AuthState.Loading)
                authRepository.signUp(email, password).await()
                _authState.emit(AuthState.Success)
            } catch (e: Exception) {
                _authState.emit(AuthState.Error(e.localizedMessage ?: "Sign up failed"))
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.emit(AuthState.Loading)
                authRepository.signIn(email, password).await()
                _authState.emit(AuthState.Success)
            } catch (e: Exception) {
                _authState.emit(AuthState.Error(e.localizedMessage ?: "Sign in failed"))
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Initial
    }


    private val _fingerprintLiveData = MutableLiveData<String?>()
    val fingerprintLiveData: LiveData<String?>
        get() = _fingerprintLiveData



    fun getFingerprintHashForUser(email: String, callback: (String?) -> Unit) {
        authRepository.getClientById(email)
        val fingerprint = authRepository.getClientFieldById(email, "fingerPrint")
        callback(fingerprint)
    }





    fun onButtonClickOpen(view: View) {
        _navigateToCollectInfos.value = true
    }

    // Function to reset navigation event after navigation
    fun onNavigationComplete() {
        _navigateToCollectInfos.value = false
    }

    val clientPartial: LogInUserPartial = LogInUserPartial()
    var email = ""
    var password=""

    fun onButtonClick(view: View) {
        viewModelScope.launch {
            email = clientPartial.email
            password=clientPartial.password
            signIn(email,password)
        }
    }


//---------------------------------------------------------------------------------







    //---------------------------------- Biometric Authentification ----------------------------------------------------



    private val _biometricEvent = MutableLiveData<BiometricViewModel.BiometricEvent>()
    val biometricEvent: LiveData<BiometricViewModel.BiometricEvent>
        get() = _biometricEvent

    private var temp: ByteArray = ByteArray(0)

    private val _showBiometricPrompt = MutableLiveData<Boolean>()
    val showBiometricPrompt: LiveData<Boolean>
        get() = _showBiometricPrompt


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
                _biometricEvent.postValue(
                    BiometricViewModel.BiometricEvent.Error(
                        errorCode,
                        errString.toString()
                    )
                )
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // Get the crypto object from the authentication result
                val cryptoObject = result.cryptoObject

                // Check if the crypto object is not null
                if (cryptoObject != null) {
                    // Use the cipher from the crypto object to encrypt your data
                    val cipher = cryptoObject.cipher
                    val fingerprintData: ByteArray? =
                        result.cryptoObject?.cipher?.doFinal("FingerprintData".toByteArray())
                    val fingerprintDataString: String =
                        fingerprintData?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                            ?: "No fingerprint data"
                    if (fingerprintData != null) {
                        temp = fingerprintData
                    }
                    Log.e("Finger Print", fingerprintDataString)
                } else {
                    // Handle the case where the crypto object is null
                    Log.e("Biometric", "Crypto object is null")
                }

            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                _biometricEvent.postValue(BiometricViewModel.BiometricEvent.Failure("Authentication failed"))
            }
        }


    fun getBiometricPrint(): ByteArray {
        return temp
    }


    private fun generateHash(data: ByteArray): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }




    fun onButtonClickAuth(view: View) {
        email = clientPartial.email
        Log.e("Email", email)
        _showBiometricPrompt.value = true

        // Wait for biometric authentication before generating fingerprint hash
        _biometricEvent.observeForever { event ->
            when (event) {
                is BiometricViewModel.BiometricEvent.Success -> {
                    // Biometric authentication succeeded, generate fingerprint hash
                    val fingerprintHash = generateHash(getBiometricPrint())
                    Log.e("FP Hash", fingerprintHash)

                    // Retrieve fingerprint hash for the user and compare
                    getFingerprintHashForUser(email) { fingerprint ->
                        if (fingerprint == null) {
                            Log.e("Failure", "Couldn't get the FP hash")
                        } else {
                            if (fingerprint == fingerprintHash) {
                                Log.e("SignIn", "SignedIn Successfully")
                            } else {
                                Log.e("SignIn", "Fingerprint mismatch")
                            }
                        }
                    }

                    // Remove observer after successful authentication
                    _biometricEvent.removeObserver { }
                }
                is BiometricViewModel.BiometricEvent.Failure -> {
                    // Handle biometric authentication failure
                    Log.e("Biometric", "Authentication failed")
                }
                else -> {
                    // Ignore other events
                }
            }
        }
    }



}





package com.example.project.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.project.models.AuthState
import com.example.project.models.LogInUserPartial
import com.example.project.oAuthRessources.SecureManager
import com.example.project.repositories.AuthRepository
import com.example.project.repositories.FCMRepository
import com.example.project.repositories.OAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val oAuthRepository: OAuthRepository,
    private val secureManager: SecureManager,
) : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var fingerPrint: String = ""
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    private lateinit var navController: NavController
    private val _navigateToCollectInfos = MutableLiveData<Boolean>()
    val navigateToCollectInfos: LiveData<Boolean> get() = _navigateToCollectInfos

    private val _navigateToDashboard = MutableLiveData<Boolean>()
    val navigateToDashboard: LiveData<Boolean> get() = _navigateToDashboard

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

    suspend fun fetchAccessToken(clientId: String, clientSecret: String): String? {
        return withContext(Dispatchers.IO) {
            val accessToken = secureManager.getAccessToken()
            if (accessToken == null) {
                try {
                    val newAccessToken = oAuthRepository.getAccessToken(clientId, clientSecret)
                    secureManager.saveAccessToken(newAccessToken)
                    Log.e("Access Token", "$newAccessToken")
                    newAccessToken
                } catch (e: Exception) {
                    Log.e("Access Token Error", e.message ?: "Failed to fetch access token")
                    null
                }
            } else {
                Log.e("Access Token", "Existing token: $accessToken")
                accessToken
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        viewModelScope.launch {
            //try {

//                val storedEmail = secureManager.getEmail()
//                val storedPassword = secureManager.getPassword()
//                val storedAccessToken = secureManager.getAccessToken()
//
//                if (email == storedEmail && password == storedPassword && storedAccessToken != null) {
                    _authState.emit(AuthState.Loading)
                    authRepository.signIn(email, password).await()
                    FirebaseAuth.getInstance().currentUser?.let { Log.d("URGENT", it.uid) }
                    FCMRepository.initializeAndSaveFcmToken()

                    Log.d("Access Token", "Client already has an access token")
                    _authState.emit(AuthState.Success)
                //} else {
                    _authState.emit(AuthState.Loading)

//                    val accessToken = fetchAccessToken(email, password)
//                    if (accessToken != null) {
//                        secureManager.saveEmail(email)
//                        secureManager.savePassword(password)
//                        secureManager.saveAccessToken(accessToken)
//                        Log.d("Access Token", "Access token newly affected to the client")
                        authRepository.signIn(email, password).await()
                        FirebaseAuth.getInstance().currentUser?.let { Log.d("URGENT", it.uid) }
                        _authState.emit(AuthState.Success)
//                    } else {
//                        _authState.emit(AuthState.Error("Failed to fetch access token"))
//                    }
//                }
//            } catch (e: Exception) {
//                _authState.emit(AuthState.Error(e.localizedMessage ?: "Sign in failed"))


    }}

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Initial
    }

    fun onButtonClickOpen(view: View) {
        _navigateToCollectInfos.value = true
    }

    fun onNavigationComplete() {
        _navigateToCollectInfos.value = false
    }

    val clientPartial: LogInUserPartial = LogInUserPartial()
    var email = ""
    var password = ""

    fun onButtonClick(view: View) {
        viewModelScope.launch {
            email = clientPartial.email
            password = clientPartial.password
            Log.d("Email", email)
            Log.d("Password", password)
            try {
                signIn(email, password)
                _navigateToDashboard.value = true
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in failed: ${e.message}")
            }
        }
    }

    fun onNavigationCompleteDash() {
        _navigateToDashboard.value = false
    }

    fun getCurrentUserUid(): String? {
        return Firebase.auth.currentUser?.uid
    }
}

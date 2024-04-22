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
import com.example.project.repositories.OAuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val oAuthRepository: OAuthRepository,
    private val secureManager: SecureManager
) : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var fingerPrint: String = ""
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    private lateinit var navController: NavController
    private val _navigateToCollectInfos = MutableLiveData<Boolean>()
    val navigateToCollectInfos: LiveData<Boolean>
        get() = _navigateToCollectInfos

    private val _navigateToDashboard = MutableLiveData<Boolean>()
    val navigateToDashboard: LiveData<Boolean>
        get() = _navigateToDashboard

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


    fun fetchAccessToken(clientId: String, clientSecret: String) {
        viewModelScope.launch {
            val accessToken = secureManager.getAccessToken()
            if (accessToken == null) {
                try {
                    val newAccessToken = oAuthRepository.getAccessToken(clientId, clientSecret)
                    secureManager.saveAccessToken(newAccessToken)
                    Log.e("Access Token", "$newAccessToken")
                } catch (e: Exception) {
                    e.message
                }
            } else {
                // Access token already exists locally
                Log.e("Access Token", "Existing token: $accessToken")
            }
        }
    }

     suspend fun signIn(email: String, password: String) {
            viewModelScope.launch {
                try {
                    val storedEmail = secureManager.getEmail()
                    val storedPassword = secureManager.getPassword()
                    if (email == storedEmail && password == storedPassword) {
                        val storedAccessToken = secureManager.getAccessToken()
                        if (storedAccessToken != null) {
                            _authState.emit(AuthState.Loading)
                            //authRepository.signIn(email, password).await()
                            _authState.emit(AuthState.Success)
                        } else {
                            // Access token does not exist, sign out
                            signOut()
                        }
                    } else {
                        // Email or password do not match, emit error state
                        _authState.emit(AuthState.Error("Invalid email or password"))
                    }
                } catch (e: Exception) {
                    _authState.emit(AuthState.Error(e.localizedMessage ?: "Sign in failed"))
                }
            }
        }


    /*
    fun fetchAccessToken(clientId: String, clientSecret: String) {
        viewModelScope.launch {
            val accessToken = secureManager.getAccessToken()
            if (accessToken == null) {
                try {
                    val newAccessToken = oAuthRepository.getAccessToken(clientId, clientSecret)
                    secureManager.saveAccessToken(newAccessToken)
                    Log.e("Access Token", "$newAccessToken")
                } catch (e: Exception) {
                    Log.e("Access Token Error", e.message ?: "Failed to fetch access token")
                }
            } else {
                // Access token already exists locally
                Log.e("Access Token", "Existing token: $accessToken")
            }
        }
    }



    suspend fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                val storedEmail = secureManager.getEmail()
                val storedPassword = secureManager.getPassword()
                val storedAccessToken = secureManager.getAccessToken()

                if (email == storedEmail && password == storedPassword && storedAccessToken != null) {
                    _authState.emit(AuthState.Loading)
                    _authState.emit(AuthState.Success)
                } else {
                    _authState.emit(AuthState.Loading)

                    val accessToken = fetchAccessToken(email,password)
                    secureManager.saveEmail(email)
                    secureManager.savePassword(password)
                    secureManager.saveAccessToken(accessToken.toString())
                    _authState.emit(AuthState.Success)
                }
            } catch (e: Exception) {
                _authState.emit(AuthState.Error(e.localizedMessage ?: "Sign in failed"))
            }
        }
    }

*/



    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Initial
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
    var password = ""

    fun onButtonClick(view: View) {
        viewModelScope.launch {
            email = clientPartial.email
            password = clientPartial.password

            try{
                signIn(email, password)
                _navigateToDashboard.value = true
            }catch(e:Exception){
                //Handling Failure ....
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
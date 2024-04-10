package com.example.project.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.project.models.AuthState
import com.example.project.models.LogInUserPartial
import com.example.project.repositories.AuthRepository
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
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
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
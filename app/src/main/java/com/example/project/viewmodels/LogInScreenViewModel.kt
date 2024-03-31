package com.example.project.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.project.models.LogInUserPartial
import com.example.project.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/*

interface AuthViewModelFactory {
    fun create(): AuthViewModel
}

class DefaultAuthViewModelFactory @Inject constructor(private val authRepository: AuthRepository) : AuthViewModelFactory {
    override fun create(): AuthViewModel {
        return AuthViewModel(authRepository)
    }
}



@HiltViewModel
class LogInScreenViewModel @Inject constructor(
    application: Application,
    private val authViewModelFactory: AuthViewModelFactory,

) : AndroidViewModel(application)  {

    val authViewModel=authViewModelFactory.create()

    val clientPartial:LogInUserPartial=LogInUserPartial()
    private lateinit var navController: NavController
    private val _navigateToCollectInfos = MutableLiveData<Boolean>()
    val navigateToCollectInfos: LiveData<Boolean>
        get() = _navigateToCollectInfos


    var email:String=""
    var password:String=""
    fun onButtonClick(view: View) {
        viewModelScope.launch {
            Log.d("Coroutine", "I entered coroutine")
            // Your asynchronous task here
            email= clientPartial.email
            password=clientPartial.password
            authViewModel.signIn(email,password)
            Log.e("Email", "email")
            Log.e("Password", "password")
            Log.e("Coroutine", "Inside coroutine")
        }
    }


    fun onButtonClickOpen(view:View) {
        _navigateToCollectInfos.value = true
    }

    // Function to reset navigation event after navigation
    fun onNavigationComplete() {
        _navigateToCollectInfos.value = false
    }

}

*/
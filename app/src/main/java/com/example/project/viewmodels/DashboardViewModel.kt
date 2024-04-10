package com.example.project.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor():ViewModel(){

    private val _navigateToVirement = MutableLiveData<Boolean>()
    val navigateToVirement: LiveData<Boolean>
        get() = _navigateToVirement


    private val _navigateToCreateAccount = MutableLiveData<Boolean>()
    val navigateToCreateAccount: LiveData<Boolean>
        get() = _navigateToCreateAccount





    fun onButtonClickCreate(view: View){
        viewModelScope.launch{
            _navigateToCreateAccount.value=true
        }
    }
    fun onButtonClick(view: View){
        viewModelScope.launch{
            _navigateToVirement.value=true
        }
    }


    fun onNavigationComplete() {
        _navigateToVirement.value = false
    }

    fun onNavigationCompleteCreateAccount() {
        _navigateToCreateAccount.value = false
    }

}
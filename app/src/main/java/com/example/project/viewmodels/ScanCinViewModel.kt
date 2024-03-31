package com.example.project.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanCinViewModel @Inject constructor():ViewModel(){


    private val _navigateToCamera = MutableLiveData<Boolean>()
    val navigateToCamera: LiveData<Boolean>
        get() = _navigateToCamera


    fun onButtonClick(view: View){
        viewModelScope.launch{
            Log.e("Coroutine","Inside coroutine")
            _navigateToCamera.value=true
        }
    }


    // Function to reset navigation event after navigation
    fun onNavigationComplete() {
        _navigateToCamera.value = false
    }

}
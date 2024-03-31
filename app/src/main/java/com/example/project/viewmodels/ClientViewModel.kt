package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project.models.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(): ViewModel() {
    private val _client= MutableLiveData<Client>()
    val client: LiveData<Client> get() =_client

    class Factory @Inject constructor() : ViewModelProvider.Factory {
         override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClientViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClientViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    fun updateClient(newClient: Client){
        _client.value=newClient
        Log.d("ClientViewModel", "Client updated: $newClient")

    }

}
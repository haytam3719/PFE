package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.models.Client
import com.example.project.repositories.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MailViewModel @Inject constructor(private val signUpRepository: SignUpRepository) : ViewModel() {

    private val _clientStoreStatus = MutableLiveData<Boolean>()
    val clientStoreStatus: LiveData<Boolean> get() = _clientStoreStatus

    fun saveClient(client: Client) {
        signUpRepository.storeClient(client)
        _clientStoreStatus.postValue(true)
    }
}
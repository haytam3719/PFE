package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.models.DeviceInfo
import com.example.project.prototype.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class VerifyCredentialsViewModel @Inject constructor(private val clientRepository: ClientRepository) : ViewModel() {

    private val _addDeviceResult = MutableLiveData<Boolean>()
    val addDeviceResult: LiveData<Boolean> = _addDeviceResult

    fun addDevice(clientUid: String, newDevice: DeviceInfo) {
        clientRepository.addDeviceToClient(clientUid, newDevice) { isSuccess ->
            _addDeviceResult.postValue(isSuccess)
        }
    }
}
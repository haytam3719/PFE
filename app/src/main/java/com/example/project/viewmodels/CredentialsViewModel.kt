package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.repositories.CredentialsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredentialsViewModel @Inject constructor(
    private val repository: CredentialsRepositoryImpl
) : ViewModel() {
    private val _secureCode = MutableLiveData<String>()
    val secureCode: LiveData<String> = _secureCode

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _operationStatus = MutableLiveData<String>()
    val operationStatus: LiveData<String> = _operationStatus

    companion object {
        private const val TAG = "CredentialsViewModel"
    }

    fun storeSecureCode(uid: String, code: String) {
        viewModelScope.launch {
            try {
                repository.storeAttijariSecureCode(uid, code)
                _operationStatus.postValue("Code successfully stored")
            } catch (e: Exception) {
                _errorMessage.postValue("Error storing code: ${e.localizedMessage}")
                Log.e(TAG, "Error storing code", e)
            }
        }
    }

    fun fetchSecureCode(uid: String) {
        viewModelScope.launch {
            repository.getAttijariSecureCodeForConnClient(uid) { code ->
                if (code == null) {
                    Log.d(TAG, "No secure code set or failed to fetch.")
                    _secureCode.postValue("")
                } else {
                    Log.d(TAG, "Fetched code: $code")
                    _secureCode.postValue(code!!)
                }
            }
        }
    }

}

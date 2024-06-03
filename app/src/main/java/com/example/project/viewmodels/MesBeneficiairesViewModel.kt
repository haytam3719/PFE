package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.ClientAccountDetails
import com.example.project.repositories.AccountRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MesBeneficiairesViewModel @Inject constructor(private val accountRepositoryImpl:AccountRepositoryImpl) : ViewModel(){

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _selectedClient = MutableLiveData<ClientAccountDetails>()
    val selectedClient: LiveData<ClientAccountDetails> = _selectedClient

    fun selectClient(client: ClientAccountDetails) {
        _selectedClient.value = client
    }


    private val _combinedDataLiveData = MutableLiveData<Result<List<ClientAccountDetails>>>()
    val combinedDataLiveData: LiveData<Result<List<ClientAccountDetails>>> get() = _combinedDataLiveData

    fun loadCombinedData(userId: String) {
        Log.d("MesBenefViewModel", "Starting loadCombinedData for userId: $userId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("MesBenefViewModel", "Launching coroutine to fetch combined data")
                val result = accountRepositoryImpl.getCombinedBeneficiaryClientDataAux(userId)
                Log.d("MesBenefViewModel", "Received result from repository: $result")

                result.onSuccess { combinedData ->
                    Log.d("MesBenefViewModel", "Combined data loaded successfully: ${combinedData.size}")
                }
                result.onFailure { exception ->
                    Log.e("MesBenefViewModel", "Error loading combined data: ${exception.message}")
                }
                _combinedDataLiveData.postValue(result)
                Log.d("MesBenefViewModel", "Posted combined data result to LiveData")
            } catch (e: Exception) {
                Log.e("MesBenefViewModel", "Error in loadCombinedData: ${e.message}")
                _combinedDataLiveData.postValue(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
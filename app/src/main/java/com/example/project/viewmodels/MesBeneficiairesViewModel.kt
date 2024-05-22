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

    private val _selectedClient = MutableLiveData<ClientAccountDetails>()
    val selectedClient: LiveData<ClientAccountDetails> = _selectedClient

    fun selectClient(client: ClientAccountDetails) {
        _selectedClient.value = client
    }


    val combinedDataLiveData = MutableLiveData<Result<List<ClientAccountDetails>>>()

    fun loadCombinedData(userId: String) {
        Log.d("MesBenefViewModel", "Starting loadCombinedData for userId: $userId")
        viewModelScope.launch {
            accountRepositoryImpl.getCombinedBeneficiaryClientData(userId) { result ->
                Log.d("MesBenefViewModel", "Received result from repository")
                result.onSuccess { combinedData ->
                    Log.d("MesBenefViewModel", "Combined data loaded successfully: ${combinedData.size}")
                }
                result.onFailure { exception ->
                    Log.e("MesBenefViewModel", "Error loading combined data: ${exception.message}")
                }
                combinedDataLiveData.postValue(result)
                Log.d("MesBenefViewModel", "Posted combined data result")
            }
        }
    }
}
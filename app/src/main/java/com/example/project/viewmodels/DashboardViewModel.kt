package com.example.project.viewmodels

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.DeviceInfo
import com.example.project.prototype.ClientRepository
import com.example.project.repositories.AccountRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val accountRepositoryImpl: AccountRepositoryImpl
):ViewModel(){

    private val _navigateToVirement = MutableLiveData<Boolean>()
    val navigateToVirement: LiveData<Boolean>
        get() = _navigateToVirement


    private val _navigateToCreateAccount = MutableLiveData<Boolean>()
    val navigateToCreateAccount: LiveData<Boolean>
        get() = _navigateToCreateAccount


    private val _navigateToPayment = MutableLiveData<Boolean>()
    val navigateToPayment: LiveData<Boolean>
        get() = _navigateToPayment


    fun onButtonClickPayment(view:View){
        viewModelScope.launch{
            _navigateToPayment.value=true
        }
    }




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

    fun onNavigationCompletePayment(){
        _navigateToPayment.value = false
    }



    private val _deviceInfoList = MutableLiveData<List<DeviceInfo>?>()
    val deviceInfoList: MutableLiveData<List<DeviceInfo>?>
        get() = _deviceInfoList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?>
        get() = _errorMessage

    fun fetchDeviceListByClientUid(uid: String) {
        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Fetching devices for UID: $uid")
                val devices = clientRepository.getClientDevices(uid)
                _deviceInfoList.postValue(devices)
                if (devices != null) {
                    Log.d("ViewModel", "Devices loaded: ${devices.size} devices found")
                } else {
                    Log.d("ViewModel", "No devices returned")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching devices: ${e.message}")
                _errorMessage.postValue("Error: ${e.message}")
            }
        }
    }

    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo.fromContext(context.applicationContext)
    }


    private val _totalBalance = MutableLiveData<Double>()
    val totalBalance: LiveData<Double> = _totalBalance

    fun loadTotalBalance(clientUid: String) {
        accountRepositoryImpl.getTotalBalanceByClientUid(clientUid) { totalBalance ->
            _totalBalance.postValue(totalBalance)
        }
    }


    private val _accountBalances = MutableLiveData<List<Pair<String, Double>>>()
    val accountBalances: LiveData<List<Pair<String, Double>>> = _accountBalances

    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> = _isDataLoaded

    fun loadAccountBalances(userId: String) {
        accountRepositoryImpl.fetchAccountBalances(userId) { balances, isSuccess ->
            _accountBalances.postValue(balances)
            _isDataLoaded.postValue(isSuccess)
        }
    }


    private val _balanceOverTime = MutableLiveData<List<Pair<String, Double>>>()
    val balanceOverTime: LiveData<List<Pair<String, Double>>> = _balanceOverTime

    fun loadBalanceOverTime(userId: String) {
        accountRepositoryImpl.fetchAccountBalancesOverTime(userId) { data ->
            _balanceOverTime.postValue(data)
        }
    }

}
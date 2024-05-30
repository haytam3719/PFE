package com.example.project.viewmodels

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.Client
import com.example.project.models.CollectClientPartial
import com.example.project.models.DeviceInfo
import com.example.project.models.SignUpState
import com.example.project.repositories.AuthRepository
import com.example.project.repositories.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class CollectInfoViewModel @Inject constructor(private val authRepository: AuthRepository, private val signUpRepository: SignUpRepository, private val clientViewModelFactory: ClientViewModel.Factory
):ViewModel(){

    private val _signUpState=MutableLiveData<SignUpState>()
    val signUpState:LiveData<SignUpState> =_signUpState

     val clientViewModel: ClientViewModel by lazy {
        clientViewModelFactory.create(ClientViewModel::class.java)
    }

    /*fun set(client: Client){
        this.client=client
    }*/

    fun signUpClient(email: String, password: String, client: Client) {
        _signUpState.postValue(SignUpState.Initial)
        viewModelScope.launch {
            try {
                _signUpState.postValue(SignUpState.Loading)
                authRepository.signUp(email, password)
                authRepository.signIn(email, password)// Assuming signUp returns UserCredential
                val clientUid: String? = authRepository.auth.uid
                if (clientUid != null) {
                    if (clientUid.isNotEmpty()) {
                        client.uid = clientUid
                        signUpRepository.storeClient(client) // Store client in the database
                        signUpRepository.getStockClientById(clientUid) // Retrieve client from the database (optional)
                        _signUpState.postValue(SignUpState.Success)
                    } else {
                        _signUpState.postValue(SignUpState.Error("Failed to get user UID"))
                    }
                }
            } catch (e: Exception) {
                _signUpState.postValue(SignUpState.Error(e.message ?: "Unknown error"))
            }
        }
    }



    fun updateClient(client: Client) {
        // Access the shared ClientViewModel instance and call its updateClient function
        clientViewModel.updateClient(client)
    }

    fun observeClient(): LiveData<Client> {
        return clientViewModel.client
    }


    //--------------------------------------------------------------------------------------------------------

    fun getDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo.fromContext(context.applicationContext)
    }

    val clientPartial: CollectClientPartial = CollectClientPartial()
    var prenom:String=""
    var nom:String=""
    var cin:String=""
    var adresse:String=""
    var date_naissance:String=""
    var tel_portable:String=""
    var domicile=""


    private val _navigateToOtp = MutableLiveData<Boolean>()
    val navigateToOtp: LiveData<Boolean>
        get() = _navigateToOtp


    fun onButtonClick(view: View){
        viewModelScope.launch{
            Log.e("Coroutine","Inside coroutine")
            prenom=clientPartial.prenom
            nom=clientPartial.nom
            cin=clientPartial.cin
            adresse=clientPartial.adresse
            date_naissance=clientPartial.date_naissance
            tel_portable=clientPartial.tel_portable
            domicile=clientPartial.domicile

            val client: Client = Client(null,nom, prenom,date_naissance, adresse, cin, domicile, tel_portable,null, emptyList(),"","","","")
            //Log.e("Client Info", "{$nom}, {$prenom},{$date_naissance},{$adresse},{$cin},{$tel_portable},{$domicile}")
            updateClient(client)
            //Log.e("Updated ViewModel",clientViewModel._client.value.toString())
            _navigateToOtp.value = true
        }
    }



    // Function to reset navigation event after navigation
    fun onNavigationComplete() {
        _navigateToOtp.value = false
    }


}

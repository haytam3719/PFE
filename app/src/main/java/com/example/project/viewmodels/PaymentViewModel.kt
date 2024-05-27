package com.example.project.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.FCM.FirebaseMessagingServiceImpl
import com.example.project.FCM.NotificationData
import com.example.project.FCM.RetrofitClient
import com.example.project.models.Bill
import com.example.project.models.CarteImpl
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse
import com.example.project.models.Virement
import com.example.project.prototype.AccountRepository
import com.example.project.prototype.PaymentRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class PaymentViewModel @Inject constructor(private val paymentRepository: PaymentRepository,private val accountRepository:AccountRepository,private var virement:Virement): ViewModel(){

    private val _bills = MutableLiveData<List<Bill>>()
    val bills: LiveData<List<Bill>> = _bills

    private val _paymentResponse = MutableLiveData<PaymentResponse>()
    val paymentResponse: LiveData<PaymentResponse> = _paymentResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    fun getBills() {
        viewModelScope.launch {
            Log.d("PaymentViewModel", "Fetching bills...")
            val result = withContext(Dispatchers.IO) {
                paymentRepository.getBills()
            }
            _bills.value = result
            Log.e("PaymentViewModel", "Bills fetched: $result")

            _error.value = "Error message"
            Log.e("Error", "Error message")

        }
    }

    fun requestPayment(paymentRequest: PaymentRequest) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                paymentRepository.requestPayment(paymentRequest)
            }
            result.onSuccess { _paymentResponse.value = it }
            result.onFailure { _error.value = it.message }
        }
    }
/*
    fun makePaiement(amount: Double, motif: String, selectedAccountId: String) {
        Log.d("makePaiement", "Called with amount: $amount, motif: $motif, selectedAccountId: $selectedAccountId")

        viewModelScope.launch {
            try {
                paymentRepository.makePaiement(amount, motif, selectedAccountId)
                Log.d("makePaiement", "Payment successful with amount: $amount, motif: $motif, selectedAccountId: $selectedAccountId")
            } catch (e: Exception) {
                Log.e("makePaiement", "Exception during makePaiement: ${e.message}")
            }
        }
    }

*/

    @RequiresApi(Build.VERSION_CODES.O)
    fun makePaiement(amount: Double, motif: String, selectedAccountId: String) {
        virement.compteBenef.numero = "ACC1"
        virement.montant = amount
        virement.motif = motif
        virement.typeTransaction = "Paiement"
        virement.motif = "Paiement"
        Log.e(
            "Before copying",
            "Emetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}"
        )
        // Fetch both compteEmet and compteBenef using callbacks
        accountRepository.getAccountByNumero(selectedAccountId) { compteEmet ->
            if (compteEmet != null) {
                accountRepository.getAccountByNumero("ACC1") { compteBenef ->
                    if (compteBenef != null) {
                        Log.e(
                            "UserInput",
                            "Emetteur: ${compteEmet}, Bénéficiaire: ${compteBenef}, Montant: ${virement.montant}, Motif: ${virement.motif}"
                        )



                        virement.effectuerVirement(
                            compteEmet,
                            compteBenef,
                            virement.montant,
                            virement.motif
                        )
                        virement.typeTransaction = "Paiement"
                        Log.e("Virement", "{$virement}")
                        accountRepository.updateAccount(compteEmet.numero, compteEmet)
                        accountRepository.updateAccount(compteBenef.numero, compteBenef)





                        Log.e(
                            "UserInput UPDATED",
                            "Emetteur: ${compteEmet}, Bénéficiaire: ${compteBenef}, Montant: ${virement.montant}, Motif: ${virement.motif}"
                        )

                    } else {
                        Log.e("Benef", "Benef null")
                    }
                }
            } else {
                Log.e("Emet", "Emet null")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun makePaiementUsingCard(amount: Double, motif: String, selectedAccountId: String, carte: CarteImpl) {
        virement.compteBenef.numero = "ACC1"
        virement.montant = amount
        virement.motif = motif
        virement.typeTransaction = "Paiement par carte bancaire"
        if(carte.numeroCompte != null) {
            virement.methodPaiement = "Carte Débit N° ${carte.numeroCarte}"
        }else{
            virement.methodPaiement = "Carte Crédit N° ${carte.numeroCarte}"
        }
        virement.motif = "Paiement"
        Log.e(
            "Before copying",
            "Emetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}"
        )
        // Fetch both compteEmet and compteBenef using callbacks
        accountRepository.getAccountByNumero(selectedAccountId) { compteEmet ->
            if (compteEmet != null) {
                accountRepository.getAccountByNumero("ACC1") { compteBenef ->
                    if (compteBenef != null) {
                        Log.e(
                            "UserInput",
                            "Emetteur: ${compteEmet}, Bénéficiaire: ${compteBenef}, Montant: ${virement.montant}, Motif: ${virement.motif}"
                        )



                        virement.effectuerVirement(
                            compteEmet,
                            compteBenef,
                            virement.montant,
                            virement.motif
                        )
                        virement.typeTransaction = "Paiement"
                        Log.e("Virement", "{$virement}")
                        accountRepository.updateAccount(compteEmet.numero, compteEmet)
                        accountRepository.updateAccount(compteBenef.numero, compteBenef)





                        Log.e(
                            "UserInput UPDATED",
                            "Emetteur: ${compteEmet}, Bénéficiaire: ${compteBenef}, Montant: ${virement.montant}, Motif: ${virement.motif}"
                        )

                    } else {
                        Log.e("Benef", "Benef null")
                    }
                }
            } else {
                Log.e("Emet", "Emet null")
            }
        }
    }



    fun handleSuccessfulPayment(body: String) {
        val apiService = RetrofitClient.instance

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM Token", "Token retrieved: $token")
                FirebaseMessagingServiceImpl.fcmToken = token

                if (token != null) {
                    val notificationData = NotificationData(
                        token = token,
                        title = "Paiement effectué avec succès",
                        body = body
                    )

                    val call = apiService.sendNotification(notificationData)
                    call.enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                println("Notification sent successfully")
                            } else {
                                println("Failed to send notification: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            println("Failed to send notification: ${t.message}")
                        }
                    })
                } else {
                    println("FCM token is null, cannot send notification")
                }
            } else {
                Log.w("FCM Token", "Fetching FCM registration token failed", task.exception)
                println("Failed to retrieve FCM token, cannot send notification")
            }
        }
    }



}







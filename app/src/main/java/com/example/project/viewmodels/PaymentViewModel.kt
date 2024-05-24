package com.example.project.viewmodels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.Bill
import com.example.project.models.CarteImpl
import com.example.project.models.PaymentRequest
import com.example.project.models.PaymentResponse
import com.example.project.models.Virement
import com.example.project.prototype.AccountRepository
import com.example.project.prototype.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    } }







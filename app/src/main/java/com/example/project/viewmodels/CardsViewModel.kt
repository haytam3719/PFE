package com.example.project.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.CarteCredit
import com.example.project.models.CarteDebit
import com.example.project.models.CarteImpl
import com.example.project.models.TransactionImpl
import com.example.project.repositories.AccountRepositoryImpl
import com.example.project.repositories.CarteRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CardsViewModel @Inject constructor(private val carteRepositoryImpl: CarteRepositoryImpl,private val accountRepositoryImpl: AccountRepositoryImpl) : ViewModel() {

    private val _cartes = MutableLiveData<List<CarteImpl>>()
    val cartes: LiveData<List<CarteImpl>> = _cartes

    private val _singleCarte = MutableLiveData<CarteImpl>()
    val singleCarte: LiveData<CarteImpl> = _singleCarte

    private val _operationStatus = MutableLiveData<Boolean>()
    val operationStatus: LiveData<Boolean> = _operationStatus

    private val _cartesByProprietaire = MutableLiveData<List<CarteImpl>>()
    val cartesByProprietaire: LiveData<List<CarteImpl>> = _cartesByProprietaire

    fun ajouterCarte(carte: CarteImpl) {
        Log.d("CardsVM", "Adding carte: ${carte.numeroCarte}")
        viewModelScope.launch {
            carteRepositoryImpl.ajouterCarte(carte, object :
                CarteRepositoryImpl.CarteRepositoryCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    _operationStatus.postValue(true)
                    Log.d("CardsVM", "Add carte operation successful")
                }

                override fun onError(error: DatabaseError) {
                    _operationStatus.postValue(false)
                    Log.e("CardsVM", "Add carte operation failed: ${error.message}")
                }
            })
        }
    }

    fun supprimerCarte(id: String) {
        viewModelScope.launch {
            carteRepositoryImpl.supprimerCarte(id, object :
                CarteRepositoryImpl.CarteRepositoryCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    _operationStatus.postValue(true)
                }

                override fun onError(error: DatabaseError) {
                    _operationStatus.postValue(false)
                }
            })
        }
    }


    fun modifierSecurityCode(id: String, newSecurityCode: String) {
        viewModelScope.launch {
            carteRepositoryImpl.modifierSecurityCode(id, newSecurityCode, object :
                CarteRepositoryImpl.CarteRepositoryCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    _operationStatus.postValue(true)
                }

                override fun onError(error: DatabaseError) {
                    _operationStatus.postValue(false)
                }
            })
        }
    }

    fun modifierCarte(carte: CarteImpl) {
        viewModelScope.launch {
            carteRepositoryImpl.modifierCarte(carte, object :
                CarteRepositoryImpl.CarteRepositoryCallback<Unit> {
                override fun onSuccess(result: Unit) {
                    _operationStatus.postValue(true)
                }

                override fun onError(error: DatabaseError) {
                    _operationStatus.postValue(false)
                }
            })
        }
    }

    fun getCarte(id: String) {
        viewModelScope.launch {
            carteRepositoryImpl.getCarte(id, object : CarteRepositoryImpl.CarteRepositoryCallback<CarteImpl> {
                override fun onSuccess(result:CarteImpl) {
                    _singleCarte.postValue(result)
                }

                override fun onError(error: DatabaseError) {

                }
            })
        }
    }

    fun getAllCartes() {
        viewModelScope.launch {
            carteRepositoryImpl.getAllCartes(object :
                CarteRepositoryImpl.CarteRepositoryCallback<List<CarteImpl>> {
                override fun onSuccess(result: List<CarteImpl>) {
                    _cartes.postValue(result)
                }

                override fun onError(error: DatabaseError) {

                }
            })
        }
    }

    fun getCartesByIdProprietaire(idProprietaire: String) {
        viewModelScope.launch {
            carteRepositoryImpl.getCartesByIdProprietaire(idProprietaire, object :
                CarteRepositoryImpl.CarteRepositoryCallback<List<CarteImpl>> {
                override fun onSuccess(result: List<CarteImpl>) {
                    _cartesByProprietaire.postValue(result)
                }

                override fun onError(error: DatabaseError) {
                    _cartesByProprietaire.postValue(emptyList())
                }
            })
        }
    }


    fun generateCreditCard(nom_titulaire:String,adresse_facturation:String): CarteCredit {
        val idCarte = UUID.randomUUID().toString()
        val id_proprietaire = FirebaseAuth.getInstance().currentUser!!.uid
        val numeroCarte = generateRandomCardNumber()
        val dateExpiration = generateRandomExpirationDate()
        val codeSecurite = Random.nextInt(100, 1000).toString()
        val limiteCredit = Random.nextDouble(1000.0, 10000.0)

        return CarteCredit(idCarte,id_proprietaire,numeroCarte,dateExpiration,codeSecurite,nom_titulaire,adresse_facturation,limiteCredit)
    }

    fun generateDebitCard(nom_titulaire:String,adresse_facturation:String,numero_compte:String): CarteDebit {
        val idCarte = UUID.randomUUID().toString()
        val id_proprietaire = FirebaseAuth.getInstance().currentUser!!.uid
        val numeroCarte = generateRandomCardNumber()
        val dateExpiration = generateRandomExpirationDate()
        val codeSecurite = Random.nextInt(100, 1000).toString()

        return CarteDebit(idCarte,id_proprietaire,numeroCarte,dateExpiration,codeSecurite,nom_titulaire,adresse_facturation,numero_compte)
    }

    private fun generateRandomCardNumber(): String {
        val builder = StringBuilder()
        for (i in 1..4) {
            builder.append(Random.nextInt(1000, 9999).toString())
            if (i < 4) builder.append(" ")
        }
        return builder.toString()
    }

    private fun generateRandomExpirationDate(): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val year = currentYear + 5
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return "${month.toString().padStart(2, '0')}/$year"
    }



    private val _transactions = MutableLiveData<List<TransactionImpl>>()
    val transactions: LiveData<List<TransactionImpl>> get() = _transactions

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchTransactionsByPaymentMethod(accountNumber: String?) {
        viewModelScope.launch {
            if(accountNumber !=null){
            val (transactions, error) = accountRepositoryImpl.fetchTransactionsByPaymentMethod(accountNumber)

            if (error == null) {
                _transactions.value = transactions!!
            } else {
                _errorMessage.value = error
            }
        }
            else{
                Log.e("CardsViewModel","It's a credit card")
            }}

    }
}

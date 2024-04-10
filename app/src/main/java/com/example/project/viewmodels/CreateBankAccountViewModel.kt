package com.example.project.viewmodels

import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.CreateBankAccount
import com.example.project.models.CompteImpl
import com.example.project.models.CreateAccountPartial
import com.example.project.prototype.Compte
import com.example.project.prototype.TypeCompte
import com.example.project.repositories.AccountRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CreateBankAccountViewModel @Inject constructor(private val accountRepositoryImpl: AccountRepositoryImpl):ViewModel(),AdapterView.OnItemSelectedListener {
    val selectedItemPosition = MutableLiveData<Int>()
    val selectedItemValue = MutableLiveData<String>()

    private val _currentUserUid = MutableLiveData<String>()

    fun getCurrentUserUid(): LiveData<String> {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            _currentUserUid.value = it
        }
        return _currentUserUid
    }



    var createAccountPartial:CreateAccountPartial = CreateAccountPartial()
     private val createBankAccount:CreateBankAccount = CreateBankAccount()
    init {
        selectedItemPosition.value = 0
        selectedItemValue.value = ""
        // Initialize selectedTypeCompte with default value or null
        createAccountPartial.type_compte.value = null
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedItemPosition.value = position
        selectedItemValue.value = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //
    }

    fun onClickButtonProvisoire(){
        val type_compte = selectedItemValue.value
        val solde = createAccountPartial.solde
        val numero = generateAccountNumber()
        val idProprietaire = getCurrentUserUid().value.toString()
        val typeCompte: TypeCompte = try {
            TypeCompte.valueOf(type_compte.toString())
        } catch (e: IllegalArgumentException) {
            TypeCompte.COURANT // Default value
        }
        val soldInitial: Double = try {
            solde.toDouble()

        } catch (e:Exception){0.0}

        val account:Compte = CompteImpl(numero,idProprietaire,typeCompte,soldInitial, mutableListOf())

        viewModelScope.launch {
            accountRepositoryImpl.createAccount(account)
        }

    }


    fun onButtonClick(view: View){
        onClickButtonProvisoire()
    }


    fun generateAccountNumber(): String {
        val randomSuffix = Random.nextInt(1000, 10000)

        val timestamp = System.currentTimeMillis()

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val timestampString = dateFormat.format(Date(timestamp))

        // Combine the prefix, timestamp, and random number to create the account number
        return "ACC$timestampString$randomSuffix"
    }
}



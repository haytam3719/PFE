package com.example.project.viewmodels

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.models.CompteImpl
import com.example.project.models.Virement
import com.example.project.prototype.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class VirementViewModel @Inject constructor(private var virement:Virement,private val accountRepository: AccountRepository): ViewModel(){
    private val _navigateToVirement = MutableLiveData<Boolean>()
    val navigateToVirement: LiveData<Boolean>
        get() = _navigateToVirement


    //Objectif : Passer le virement au fragment pour le data binding
    private val _virementLiveData = MutableLiveData<Virement>()
    val virementLiveData: LiveData<Virement> = _virementLiveData





    init {
        _virementLiveData.value = virement
        Log.e("ViewModel", "VirementLiveData updated with value: ${virement.toString()}")

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickProvisoire(emetteur:CompteImpl, beneficiaire:CompteImpl, montant:Double, motif:String){

        virement.effectuerVirement(emetteur, beneficiaire, montant, motif)
        Log.e("Virement","{$virement}")
        accountRepository.updateAccount(emetteur.numero,emetteur)
        accountRepository.updateAccount(beneficiaire.numero,beneficiaire)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onButtonClick(view: View){
        Log.e("Before copying", "Emetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}")
        // Fetch both compteEmet and compteBenef using callbacks
        accountRepository.getAccountByNumero(virement.compteEmet.numero) { compteEmet ->
            if (compteEmet != null) {
                accountRepository.getAccountByNumero(virement.compteBenef.numero) { compteBenef ->
                    if (compteBenef != null) {
                        Log.e("UserInput", "Emetteur: ${compteEmet}, Bénéficiaire: ${compteBenef}, Montant: ${virement.montant}, Motif: ${virement.motif}")
                        onClickProvisoire(compteEmet, compteBenef, virement.montant, virement.motif)
                        Log.e("UserInput UPDATED", "Emetteur: ${compteEmet}, Bénéficiaire: ${compteBenef}, Montant: ${virement.montant}, Motif: ${virement.motif}")

                    } else {
                        Log.e("Benef","Benef null")
                    }
                }
            } else {
                Log.e("Emet","Emet null")
            }
        }





    }


    fun onNavigationComplete() {
      //
    }

}
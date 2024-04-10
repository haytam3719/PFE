package com.example.project.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.project.modules.CompteModule
import javax.inject.Inject
import javax.inject.Named

class Virement @Inject constructor(
    @Named("idTran") idTran: String,
    @Named("montant") montant: Double,
    @Named("date") date: String,
    @Named("motif") motif: String,
    @CompteModule.CompteBenefQualifier compteBenef: CompteImpl,
    @CompteModule.CompteEmetQualifier compteEmet: CompteImpl,
    @Named("statut") statut: String,
    @Named("methodPaiement") methodPaiement: String,
    @Named("fraisTrans") fraisTrans: Double,
    @Named("typeTransaction") typeTransaction: String
) : TransactionImpl(idTran, montant, date, motif, compteBenef, compteEmet, statut, methodPaiement, fraisTrans, typeTransaction) {


    constructor() : this(
        idTran = "",
        montant = 0.0,
        date = "",
        motif = "",
        compteBenef = CompteImpl(),
        compteEmet = CompteImpl(), 
        statut = "",
        methodPaiement = "",
        fraisTrans = 0.0,
        typeTransaction = "Virement"
    )

    
    @RequiresApi(Build.VERSION_CODES.O)
    fun effectuerVirement(emetteur:CompteImpl, beneficiaire:CompteImpl, montant: Double, motif: String){
        emetteur.retrait(montant)
        beneficiaire.depot(montant)
        this.compteEmet.id_proprietaire = emetteur.id_proprietaire
        this.compteEmet.solde = emetteur.solde
        this.compteEmet.type = emetteur.type

        this.compteBenef.id_proprietaire = beneficiaire.id_proprietaire
        this.compteBenef.solde = beneficiaire.solde
        this.compteBenef.type = beneficiaire.type

        this.motif = motif
        this.typeTransaction = "Virement"
        enregistrerTransaction(this)
        emetteur.historiqueTransactions.add(this)
        beneficiaire.historiqueTransactions.add(this)
    }


    /*
    fun getDetailsVirement(): String {
        val transactionInfo = getTransactionInfos(this)
        return "Details of Virement Transaction:\n$transactionInfo"
    }

     override fun toString(): String {
        return "Virement[idTran=$idTran, montant=$montant, date=$date, motif=$motif, compteBenef=${compteBenef.numero}, compteEmet=${compteEmet.numero}, statut=$statut, methodPaiement=$methodPaiement, fraisTrans=$fraisTrans, typeTransaction=$typeTransaction]"
    }

     */


}


//this.compteEmet = emetteur
//this.compteBenef = beneficiaire

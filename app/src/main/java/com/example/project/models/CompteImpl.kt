package com.example.project.models

import com.example.project.prototype.Compte
import com.example.project.prototype.TypeCompte
import javax.inject.Inject
import javax.inject.Named

data class CompteImpl @Inject constructor(
    @Named("numero") override var numero: String,
    @Named("id_proprietaire") override var id_proprietaire: String,
    @Named("typeCompte") override var type: TypeCompte,
    @Named("solde") override var solde: Double,
    override val historiqueTransactions: MutableList<TransactionImpl> = mutableListOf(),


    ) : Compte{

    constructor() : this("", "", TypeCompte.COURANT, 0.0, mutableListOf())

    fun estSuffisant(montant: Double):Boolean{
        return solde>=montant
    }

    override fun depot(montant: Double) {
        if (montant > 0) {
            solde += montant
        } else {
            throw IllegalArgumentException("Le montant du dépôt doit être supérieur à zéro.")
        }
    }

    override fun retrait(montant: Double) {
        if (montant > 0 && estSuffisant(montant)) {
            solde -= montant
        } else {
            throw IllegalArgumentException("Le montant du retrait doit être supérieur à zéro et inférieur ou égal au solde.")
        }
    }


    override fun consulterSolde(): Double {
        return solde
    }


    override fun toMap(): Map<String, Any> {
        return mapOf(
            "numero" to numero,
            "id_proprietaire" to id_proprietaire,
            "type" to type.name, // Convert TypeCompte enum to its name
            "solde" to solde,
            "historiqueTransactions" to historiqueTransactions.map { it.toMap() } // Convert list of transactions to list of maps
        )
    }



}

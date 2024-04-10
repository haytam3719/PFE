package com.example.project.prototype

import com.example.project.models.TransactionImpl

enum class TypeCompte {
    COURANT,
    EPARGNE,
    CHEQUES
}
interface Compte {
    val numero: String
    val id_proprietaire: String
    val type: TypeCompte
    var solde: Double
    val historiqueTransactions: MutableList<TransactionImpl>

    fun depot(montant: Double)
    fun retrait(montant: Double)

    fun consulterSolde():Double
    fun toMap(): Map<String, Any>

}



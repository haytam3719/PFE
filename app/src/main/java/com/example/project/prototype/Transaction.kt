package com.example.project.prototype

import com.example.project.models.CompteImpl

interface Transaction {
    var idTran: String
    var montant: Double
    var date: String
    var motif: String
    var compteBenef: CompteImpl
    var compteEmet: CompteImpl
    var statut: String
    var methodPaiement: String
    var fraisTrans: Double
    var typeTransaction: String

    fun enregistrerTransaction(transaction:Transaction)
    fun getTransactionInfos(transaction:Transaction):String
    fun toMap(): Map<String, Any>
}

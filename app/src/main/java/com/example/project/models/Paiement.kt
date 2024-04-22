package com.example.project.models


import android.os.Build
import androidx.annotation.RequiresApi

class Paiement(
    idTran: String,
    montant: Double,
    date: String,
    motif: String,
    compteBenef: CompteImpl,
    compteEmet: CompteImpl,
    statut: String,
    methodPaiement: String,
    fraisTrans: Double,
    typeTransaction: String,
    private val virement: Virement
) : TransactionImpl(
    idTran,
    montant,
    date,
    motif,
    compteBenef,
    compteEmet,
    statut,
    methodPaiement,
    fraisTrans,
    typeTransaction
) {

    constructor() : this(
        "",
        0.0,
        "",
        "",
        CompteImpl(),
        CompteImpl(),
        "",
        "",
        0.0,
        "",
        Virement()
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun makePayment(amount: Double, fromAccount: CompteImpl, toAccount: CompteImpl, motif: String) {
        this.montant = amount
        this.compteEmet = fromAccount
        this.compteBenef = toAccount
        this.typeTransaction = "Paiement"
        this.motif = motif
        virement.effectuerVirement(fromAccount,toAccount,amount,motif)
        this.enregistrerTransaction(this)

        //val paymentRequest = PaymentRequest(amount, fromAccount.numero, toAccount.numero, method)

    }
    }

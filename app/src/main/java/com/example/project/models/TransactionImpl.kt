package com.example.project.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.project.modules.CompteModule
import com.example.project.prototype.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named


open class TransactionImpl @Inject constructor(
    @Named("idTran") override var idTran: String,
    @Named("montant") override var montant: Double,
    @Named("date") override var date: String,
    @Named("motif") override var motif: String,
    @CompteModule.CompteBenefQualifier override var compteBenef: CompteImpl,
    @CompteModule.CompteEmetQualifier override var compteEmet: CompteImpl,
    @Named("statut") override var statut: String,
    @Named("methodPaiement") override var methodPaiement: String,
    @Named("fraisTrans") override var fraisTrans: Double,
    @Named("typeTransaction") override var typeTransaction: String,

    ) : Transaction{

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
        typeTransaction = ""
    )


    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @RequiresApi(Build.VERSION_CODES.O)
     override fun enregistrerTransaction(transaction: Transaction){
         this.setIdTran()
         this.setDate()

         this.setStatut()
    }

    override fun getTransactionInfos(transaction: Transaction):String {
        return this.toString()
    }


    override fun toMap(): Map<String, Any> {
        return mapOf(
            "idTran" to idTran,
            "montant" to montant,
            "date" to date,
            "motif" to motif,
            "statut" to statut,
            "compteBenef" to compteBenef.toMap(), // Convert compteBenef to map
            "compteEmet" to compteEmet.toMap(),   // Convert compteEmet to map
            "methodPaiement" to methodPaiement,
            "fraisTrans" to fraisTrans,
            "typeTransaction" to typeTransaction
        )
    }

    private fun setIdTran(){
        val uuid = UUID.randomUUID()

        // Convert UUID to string and remove hyphens
        val id = uuid.toString().replace("-", "")

        val timestamp = System.currentTimeMillis()
        this.idTran = "$id-$timestamp"
    }

    private fun setDate() {
        val currentDate = Date()
        this.date = dateFormatter.format(currentDate)
    }

    private fun setStatut(){
        val pattern = Regex("^ACC\\d{14}\\d{4}$")
        if(!pattern.matches(compteBenef.numero)){
            this.statut = "Externe"
        }else{
            this.statut = "Interne"
        }
    }




    }





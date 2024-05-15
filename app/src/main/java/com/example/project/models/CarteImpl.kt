package com.example.project.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.project.prototype.Carte
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

open class CarteImpl(
    override var idCarte: String,
    override val id_proprietaire_carte: String,
    override val numeroCarte: String,
    override val dateExpiration: String,
    override var codeSecurite: String,
    override val nomTitulaire: String,
    override val adresseFacturation: String,
    open var limiteCredit: Double? = null,
    open var numeroCompte: String? = null
) : Carte {


    constructor() : this(
        idCarte = "",
        id_proprietaire_carte = "",
        numeroCarte = "0000 0000 0000 0000",
        dateExpiration = "01/00",
        codeSecurite = "000",
        nomTitulaire = "N/A",
        adresseFacturation = "No Address Provided",
        limiteCredit = null,
        numeroCompte = null
    )


    private var estBloquee: Boolean = false

    override fun bloquerCarte() {
        this.estBloquee = true
    }

    override fun debloquerCarte() {
        this.estBloquee = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun verifierValiditeCarte(): Boolean {
        try {
            val dateFormatter = DateTimeFormatter.ofPattern("MM/yy")
            val dateExpirationCarte = LocalDate.parse(dateExpiration, dateFormatter)
            val dateCourante = LocalDate.now()
            return !estBloquee && dateCourante.isBefore(dateExpirationCarte)
        } catch (e: DateTimeParseException) {
            return false
        }
    }

    override fun modifierCodeSecurite(nouveauCode: String) {
        this.codeSecurite = nouveauCode
    }
}

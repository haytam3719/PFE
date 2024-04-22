package com.example.project.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.project.prototype.Carte
import java.time.LocalDate

open class CarteImpl(
    override var idCarte: String,
    override val id_proprietaire_carte: String,
    override val numeroCarte: String,
    override val dateExpiration: String,
    override var codeSecurite: String,
    override val nomTitulaire: String,
    override val adresseFacturation: String
) : Carte {

    private var estBloquee: Boolean = false

    override fun bloquerCarte() {
        this.estBloquee = true
    }

    override fun debloquerCarte() {
        this.estBloquee = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun verifierValiditeCarte(): Boolean {
        val dateCourante = LocalDate.now()
        val dateExpirationCarte = LocalDate.parse(dateExpiration)
        return !estBloquee && dateCourante.isBefore(dateExpirationCarte)
    }

    override fun modifierCodeSecurite(nouveauCode: String) {
        this.codeSecurite = nouveauCode
    }
}

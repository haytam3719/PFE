package com.example.project.prototype

interface Carte {
    var idCarte: String
    val id_proprietaire_carte: String
    val numeroCarte: String
    val dateExpiration: String
    val codeSecurite: String
    val nomTitulaire: String
    val adresseFacturation: String

    fun bloquerCarte()

    fun debloquerCarte()

    fun verifierValiditeCarte(): Boolean

    fun modifierCodeSecurite(nouveauCode: String)
}

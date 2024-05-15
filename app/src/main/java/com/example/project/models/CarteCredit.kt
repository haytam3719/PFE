package com.example.project.models

class CarteCredit(
    idCarte: String,
    id_proprietaire: String,
    numeroCarte: String,
    dateExpiration: String,
    codeSecurite: String,
    nomTitulaire: String,
    adresseFacturation: String,
    override var limiteCredit: Double?
) : CarteImpl(idCarte, id_proprietaire, numeroCarte, dateExpiration, codeSecurite, nomTitulaire, adresseFacturation){
    private val virement = Virement()



}

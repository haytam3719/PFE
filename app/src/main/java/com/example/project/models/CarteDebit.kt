package com.example.project.models

class CarteDebit(
    idCarte: String,
    id_proprietaire_carte: String,
    numeroCarte: String,
    dateExpiration: String,
    codeSecurite: String,
    nomTitulaire: String,
    adresseFacturation: String,
    override var numeroCompte: String?
) : CarteImpl(idCarte,id_proprietaire_carte, numeroCarte, dateExpiration, codeSecurite, nomTitulaire, adresseFacturation)

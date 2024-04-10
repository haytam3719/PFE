package com.example.project.prototype

interface AccountCreationService {
    fun createAccount(numero: String, idProprietaire: String, typeCompte: TypeCompte, soldeInitial: Double): Compte
}
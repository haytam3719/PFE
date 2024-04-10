package com.example.project.models

import com.example.project.prototype.AccountCreationService
import com.example.project.prototype.Compte
import com.example.project.prototype.TypeCompte
import javax.inject.Inject

class AccountCreationServiceImpl @Inject constructor() : AccountCreationService {
    override fun createAccount(numero: String, idProprietaire: String, typeCompte: TypeCompte, soldeInitial: Double): Compte {
        return CompteImpl(numero, idProprietaire, typeCompte, soldeInitial, mutableListOf())
    }
}
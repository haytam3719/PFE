package com.example.project.prototype

import com.example.project.models.CompteImpl

interface AccountRepository {
    suspend fun getAccounts(): List<Compte>
    suspend fun createAccount(account: Compte)
    suspend fun deleteAccountByNumero(numero: String)
    fun updateAccount(accountId: String, updatedAccount: Compte)
    fun getAccountByNumero(numero: String, callback: (CompteImpl?) -> Unit)

}
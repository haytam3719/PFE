package com.example.project.prototype

interface CredentialsRepository {
    val id_client:String
    val code_attijariSecure:String

    suspend fun storeAttijariSecureCode(uid: String, code: String)
    fun getAttijariSecureCodeForConnClient(uid: String, callback: (String?) -> Unit)}
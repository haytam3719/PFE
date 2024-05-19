package com.example.project.oAuthRessources

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SecureManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
    private val accessTokenKey = "accessToken"
    private val emailKey = "email"
    private val passwordKey = "password"

    // Use a 16-byte key for AES-128 encryption
    private val secretKey = "16byte_secretkey"

    fun saveAccessToken(accessToken: String) {
        val encryptedToken = encrypt(accessToken)
        sharedPreferences.edit().putString(accessTokenKey, encryptedToken).apply()
    }

    fun getAccessToken(): String? {
        val encryptedToken = sharedPreferences.getString(accessTokenKey, null)
        return if (encryptedToken != null) decrypt(encryptedToken) else null
    }

    fun saveEmail(email: String) {
        val encryptedEmail = encrypt(email)
        sharedPreferences.edit().putString(emailKey, encryptedEmail).apply()
    }

    fun getEmail(): String? {
        val encryptedEmail = sharedPreferences.getString(emailKey, null)
        return if (encryptedEmail != null) decrypt(encryptedEmail) else null
    }

    fun savePassword(password: String) {
        val encryptedPassword = encrypt(password)
        sharedPreferences.edit().putString(passwordKey, encryptedPassword).apply()
    }

    fun getPassword(): String? {
        val encryptedPassword = sharedPreferences.getString(passwordKey, null)
        return if (encryptedPassword != null) decrypt(encryptedPassword) else null
    }

    private fun encrypt(input: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey.toByteArray(), "AES"))
        val encryptedBytes = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decrypt(input: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey.toByteArray(), "AES"))
        val encryptedBytes = Base64.decode(input, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}

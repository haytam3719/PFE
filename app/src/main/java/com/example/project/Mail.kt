package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.project.databinding.MailBinding
import com.example.project.models.Client
import com.example.project.models.EmailRequest
import com.example.project.models.EmailResponse
import com.example.project.models.MailApiClient
import com.example.project.models.MailApiService
import com.example.project.viewmodels.CollectInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.SecureRandom

@AndroidEntryPoint
class Mail : Fragment() {

    private var _binding: MailBinding? = null
    private val binding get() = _binding!!
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })

    private var generatedEmail: String? = null
    private var generatedPassword: String = generateStrongPassword(8)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val topAppBar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        collectInfoViewModel.observeClient().observe(viewLifecycleOwner, Observer { client ->
            if (client != null) {
                binding.button.setOnClickListener {
                    val email = binding.mailTextInputLayout.editText?.text.toString()
                    if (email.isNotEmpty()) {
                        if (generatedEmail == null) {
                            generatedEmail = email
                        }
                        sendEmailWithCredentials(generatedEmail!!, generatedPassword, client)
                    } else {
                        Toast.makeText(context, "Adresse Email invalide", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Client object is null", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sendEmailWithCredentials(email: String, password: String, client: Client) {
        val apiService = MailApiClient.retrofit.create(MailApiService::class.java)
        val imageUrl = "https://historiadelaempresa.com/wp-content/uploads/logotipo/Attijariwafa-Bank.png"

        val emailContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ouverture de votre compte</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        margin: 0;
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        padding: 20px;
                        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        border-radius: 8px;
                    }
                    .title {
                        font-size: 24px;
                        font-weight: bold;
                        color: #333333;
                        margin-bottom: 10px;
                    }
                    .content {
                        font-size: 16px;
                        color: #555555;
                        margin-bottom: 20px;
                    }
                    .footer img {
                        max-width: 100%;
                        height: auto;
                        display: block;
                        margin: 0 auto;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="title">Ouverture de votre compte</div>
                    <div class="content">
                        <p>Félicitations, vous êtes désormais client d'Attijariwafa bank. Pour vous connecter, veuillez utiliser l'identifiant et le mot de passe suivants :</p>
                        <ul>
                            <li><strong>Identifiant :</strong> $email</li>
                            <li><strong>Mot de passe :</strong> $password</li>
                        </ul>
                        <p>Nous vous remercions de votre confiance et restons à votre disposition pour toute question ou assistance.</p>
                    </div>
                    <div class="footer">
                        <img src="$imageUrl" alt="Footer Image">
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        val request = EmailRequest(email = email, subject = "Ouverture de compte Attijariwafa", content = emailContent)

        CoroutineScope(Dispatchers.IO).launch {
            apiService.sendEmail(request).enqueue(object : Callback<EmailResponse> {
                override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                    if (response.isSuccessful) {
                        // Email sent successfully, proceed to create user in Firebase
                        createUserInFirebase(email, password, client)
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Erreur de connexion: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun createUserInFirebase(email: String, password: String, client: Client) {
        collectInfoViewModel.signUpClient(email, password, client)
    }

    private fun generateStrongPassword(length: Int): String {
        val upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val specialCharacters = "!@#\$%^&*()-_+=<>?/{}~|"

        val allCharacters = upperCaseLetters + lowerCaseLetters + digits + specialCharacters
        val random = SecureRandom()

        val password = StringBuilder(length)
        password.append(upperCaseLetters[random.nextInt(upperCaseLetters.length)])
        password.append(lowerCaseLetters[random.nextInt(lowerCaseLetters.length)])
        password.append(digits[random.nextInt(digits.length)])
        password.append(specialCharacters[random.nextInt(specialCharacters.length)])

        for (i in 4 until length) {
            password.append(allCharacters[random.nextInt(allCharacters.length)])
        }

        return password.toString().toList().shuffled().joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.project.databinding.MailBinding
import com.example.project.models.EmailRequest
import com.example.project.models.EmailResponse
import com.example.project.models.MailApiClient
import com.example.project.models.MailApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Mail : Fragment() {

    private var _binding: MailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = MailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            val email = binding.mailTextInputLayout.editText?.text.toString()
            if (email.isNotEmpty()) {
                sendEmail(email)
            } else {
                Toast.makeText(context, "Adresse Email invalide", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendEmail(toEmail: String) {
        val apiService = MailApiClient.retrofit.create(MailApiService::class.java)
        val request = EmailRequest(email = toEmail)

        CoroutineScope(Dispatchers.IO).launch {
            apiService.sendEmail(request).enqueue(object : Callback<EmailResponse> {
                override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Mail envoyé avec succès", Toast.LENGTH_LONG).show()
                        } else {
                            Log.e("MailFragment", "Failed to send email: ${response.message()}")
                            Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.e("MailFragment", "Network error: ${t.message}", t)
                        Toast.makeText(context, "Erreur de connexion: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

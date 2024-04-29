package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project.databinding.MailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

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
                sendEmailViaSandbox(email)
            } else {
                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendEmailViaSandbox(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val postData = buildString {
                append("from=Mailgun Sandbox <postmaster@sandbox4feba5ef38d1408088542a3dd61590da.mailgun.org>")
                append("&to=$email")
                append("&subject=Hello from Mailgun Sandbox")
                append("&text=This is a test email sent from the Mailgun Sandbox!")
            }

            val body = RequestBody.create(mediaType, postData)
            val request = Request.Builder()
                .url("https://api.mailgun.net/v3/sandbox4feba5ef38d1408088542a3dd61590da.mailgun.org/messages")
                .post(body)
                .addHeader("Authorization", "Basic " + Credentials.basic("api", "2175ccc2-34dc43b5"))
                .build()

            client.newCall(request).execute().use { response ->
                withContext(Dispatchers.Main) {
                    if (!response.isSuccessful) {
                        Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Email sent successfully", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

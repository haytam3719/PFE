package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.AttijariSecureBinding
import com.example.project.viewmodels.CredentialsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttijariSecure : Fragment() {
    private var _binding: AttijariSecureBinding? = null
    private val binding get() = _binding!!
    private val credentialsViewModel: CredentialsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AttijariSecureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        lifecycleScope.launch {
            credentialsViewModel.fetchSecureCode(uid)
        }

        credentialsViewModel.secureCode.observe(viewLifecycleOwner) { secureCode ->
            if (secureCode.isNullOrEmpty()) {
                binding.tvCodeAttijari.text = "Définir un code Attijari Secure"
                binding.definirAttijariSecure.setOnClickListener {
                    findNavController().navigate(R.id.attijariSecure_to_credentials)
                }
            } else {
                binding.tvCodeAttijari.text = "Modifier le code Attijari Secure"
                binding.definirAttijariSecure.setOnClickListener {
                    findNavController().navigate(R.id.attijariSecure_to_verifyCredentials)

                }
            }
        }

        credentialsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }

        binding.deviceButton.setOnClickListener {
            findNavController().navigate(R.id.attijariSecure_to_deviceInfo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.VerifierCodeAttijariSecureBinding
import com.example.project.viewmodels.CredentialsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyCredentials : Fragment() {

    private var _binding: VerifierCodeAttijariSecureBinding? = null
    private val binding get() = _binding!!
    private val credentialsViewModel: CredentialsViewModel by viewModels()
    private var shouldTrustDevice:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VerifierCodeAttijariSecureBinding.inflate(inflater, container, false)
        binding.verifyCredentials = this
        shouldTrustDevice = arguments?.getBoolean("shouldTrustDevice", false) ?: false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    fun onClickVerifier(view: View) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            credentialsViewModel.secureCode.observe(viewLifecycleOwner, Observer { fetchedSecureCode ->
                val userInputCode = binding.verifyAttijariSecureTextInputLayout.editText?.text.toString()

                if (fetchedSecureCode == userInputCode) {
                    Log.d("Verification", "The codes match: true")
                    if (shouldTrustDevice) {
                        findNavController().navigate(R.id.verifyCredentials_to_deviceInfoList)
                    } else {
                        findNavController().navigate(R.id.verifyCredentials_to_credentials)
                    }
                } else {
                    Log.d("Verification", "The codes do not match: false")
                    Toast.makeText(context, "Le code est erroné.", Toast.LENGTH_SHORT).show()

                }

                //credentialsViewModel.secureCode.removeObservers(viewLifecycleOwner)
            })

            lifecycleScope.launch {
                credentialsViewModel.fetchSecureCode(uid)
            }
        } else {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

}
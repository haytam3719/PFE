package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.CodeAttijariSecureBinding
import com.example.project.viewmodels.CredentialsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Credentials : Fragment(){
    private var _binding: CodeAttijariSecureBinding? = null
    private val binding get() = _binding!!
    private val credentialsViewModel:CredentialsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CodeAttijariSecureBinding.inflate(inflater, container, false)
        binding.credentials = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun onClickValider(view:View){
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val code = binding.attijariSecureTextInputLayout.editText?.text.toString()
        if (uid != null && code.isNotEmpty()) {
            credentialsViewModel.storeSecureCode(uid, code)
        } else {
            Toast.makeText(context, "Code or user ID is missing", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.CreateAccountBinding
import com.example.project.viewmodels.CreateBankAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateBankAccount : Fragment(){
    private val createBankAccountViewModel:CreateBankAccountViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CreateAccountBinding.inflate(inflater, container, false)

        binding.createAccountPartial=createBankAccountViewModel.createAccountPartial
        binding.createBankAcountViewModel=createBankAccountViewModel

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root

    }

}
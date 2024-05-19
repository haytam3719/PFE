package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.Payment3Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStepThree :Fragment() {

    private var _binding: Payment3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Payment3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun onClickContinuer(view:View){
        findNavController().navigate(R.id.paymentStepThree_to_paymentStepFour)
    }
}
package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.Payment2Binding
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStepTwo : Fragment() {

    private var _binding: Payment2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Payment2Binding.inflate(inflater, container, false)
        binding.paymentStepTwo = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

    }

    fun onClickContinuer(view:View){
        findNavController().navigate(R.id.paymentStepTwo_to_paymentStepThree)
    }
}
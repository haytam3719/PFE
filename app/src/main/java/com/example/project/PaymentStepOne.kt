package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.Payment1Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStepOne : Fragment() {

    private var _binding: Payment1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Payment1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageViewList = listOf(
            binding.ivIcon4, binding.ivIcon5, binding.ivIcon6,
            binding.ivIcon7, binding.ivIcon8, binding.ivIcon9
        )

        for (imageView in imageViewList) {
            imageView.setOnClickListener {
                findNavController().navigate(R.id.paymentStepOne_to_paymentStepTwo)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
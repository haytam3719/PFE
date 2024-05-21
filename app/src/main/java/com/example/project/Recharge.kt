package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.MontantRechargeAdapter
import com.example.project.databinding.RechargeSimpleBinding
import com.example.project.viewmodels.RechargeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Recharge : Fragment(), MontantRechargeAdapter.OnAmountSelectedListener {

    private var _binding: RechargeSimpleBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MontantRechargeAdapter
    private val amounts = listOf("10 DH", "20 DH", "50 DH", "100 DH", "200 DH", "300 DH", "500 DH", "1000 DH")
    private val rechargeViewModel: RechargeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RechargeSimpleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MontantRechargeAdapter(amounts, this)
        binding.recyclerViewAmounts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAmounts.adapter = adapter

        binding.buttonConfirm.setOnClickListener {
            val selectedAmount = adapter.getSelectedAmount()
            if (selectedAmount != null) {
                Log.d("Recharge", "Selected amount: $selectedAmount")

                binding.cardViewMontantOptions.visibility = View.GONE
                binding.tvMontant.text = "Montant sélectionné: $selectedAmount"
                rechargeViewModel.setMontant(selectedAmount)

                binding.iconEndMontant.visibility = View.GONE
                binding.continuerButton.visibility = View.VISIBLE

            } else {
                Toast.makeText(requireContext(), "Veuillez sélectionner un item", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCancel.setOnClickListener {
            binding.cardViewMontantOptions.visibility = View.GONE
            binding.continuerButton.visibility = View.VISIBLE
        }

        binding.cardViewMontant.setOnClickListener {
            binding.cardViewMontantOptions.visibility = View.VISIBLE
            binding.continuerButton.visibility = View.GONE
        }

        binding.continuerButton.setOnClickListener {
            val phoneNumber = binding.textInputLayoutPhoneNumber.editText?.text.toString()
            if (phoneNumber.isBlank() || binding.tvMontant.text.isBlank()) {
                Toast.makeText(requireContext(), "Veuillez renseigner tous les champs", Toast.LENGTH_LONG).show()
            }

            Log.d("Recharge", "Setting phone number: $phoneNumber")
            Log.d("Recharge", "Setting montant: ${binding.tvMontant.text}")
            rechargeViewModel.setPhoneNumber(phoneNumber)
            findNavController().navigate(R.id.recharge_to_recharge2)

        }
    }

    override fun onAmountSelected(amount: String) {
        // Handle amount selection
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

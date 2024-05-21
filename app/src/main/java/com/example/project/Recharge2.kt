package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.MontantRechargeAdapter
import com.example.project.databinding.RechargeSimple2Binding
import com.example.project.viewmodels.RechargeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Recharge2 : Fragment(), MontantRechargeAdapter.OnAmountSelectedListener {

    private var _binding: RechargeSimple2Binding? = null
    private val binding get() = _binding!!

    private val rechargeViewModel: RechargeViewModel by activityViewModels()

    private lateinit var adapter: MontantRechargeAdapter
    private val rechargeTypes = listOf("Recharge Normale", "Pass SMS", "Pass National", "Pass Internet", "Pass Réseaux Sociaux")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RechargeSimple2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MontantRechargeAdapter(rechargeTypes, this)
        binding.recyclerViewTypeRecharge.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTypeRecharge.adapter = adapter

        // Log for ViewModel observation
        rechargeViewModel.rechargeType.observe(viewLifecycleOwner) { rechargeType ->
            Log.d("Recharge2", "Recharge Type observed: $rechargeType")
            binding.tvTypeRecharge.text = "Type de recharge sélectionné: $rechargeType"
        }

        binding.buttonConfirm.setOnClickListener {
            val selectedType = adapter.getSelectedAmount()
            if (selectedType != null) {
                Log.d("Recharge2", "Selected recharge type: $selectedType")
                binding.cardViewTypeRechargeOptions.visibility = View.GONE
                binding.tvTypeRecharge.text = "Type de recharge sélectionné: $selectedType"
                rechargeViewModel.setRechargeType(selectedType)
                binding.iconEndTypeRecharge.visibility = View.GONE
                binding.continuerButton.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "Veuillez sélectionner un type", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCancel.setOnClickListener {
            binding.cardViewTypeRechargeOptions.visibility = View.GONE
            binding.continuerButton.visibility = View.VISIBLE
        }

        binding.cardViewTypeRecharge.setOnClickListener {
            binding.continuerButton.visibility = View.GONE
            binding.cardViewTypeRechargeOptions.visibility = View.VISIBLE
        }

        binding.continuerButton.setOnClickListener {
            if (!binding.tvTypeRecharge.text.toString().contains(":")) {
                Toast.makeText(requireContext(), "Veuillez choisir un type", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.recharge2_to_recharge3)
            }
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

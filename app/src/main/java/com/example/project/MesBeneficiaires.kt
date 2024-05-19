package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.ClientAdapter
import com.example.project.databinding.MesBeneficiairesBinding
import com.example.project.viewmodels.ConsultationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MesBeneficiaires : Fragment() {
    private val consultationViewModel:ConsultationViewModel by viewModels()
    private var _binding: MesBeneficiairesBinding? = null
    private lateinit var adapterBeneficiary: ClientAdapter
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapterBeneficiary = ClientAdapter(emptyList(),consultationViewModel)

        binding.recyclerViewBeneficiaries.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewBeneficiaries.adapter = adapterBeneficiary

        consultationViewModel.combinedDataLiveData.observe(viewLifecycleOwner) { result ->
            result.onSuccess { combinedData ->
                Log.d("Fragment", "Combined data received: $combinedData")
                adapterBeneficiary.updateClientDetails(combinedData)
            }
            result.onFailure { exception ->
                Log.e("Fragment", "Error receiving combined data: ${exception.message}")
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        consultationViewModel.loadCombinedData(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MesBeneficiairesBinding.inflate(inflater, container, false)
        return binding.root
    }
}
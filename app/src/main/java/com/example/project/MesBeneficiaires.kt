package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.ClientAdapter
import com.example.project.databinding.MesBeneficiairesBinding
import com.example.project.viewmodels.ConsultationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MesBeneficiaires : Fragment() {
    private val consultationViewModel: ConsultationViewModel by activityViewModels()
    private var _binding: MesBeneficiairesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterBeneficiary: ClientAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MesBeneficiairesBinding.inflate(inflater, container, false)
        adapterBeneficiary = ClientAdapter(emptyList(), consultationViewModel)
        binding.recyclerViewBeneficiaries.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewBeneficiaries.adapter = adapterBeneficiary
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        consultationViewModel.combinedDataLiveData.observe(viewLifecycleOwner) { result ->
            result.onSuccess { combinedData ->
                Log.d("Fragment", "Combined data received: $combinedData")
                if (combinedData.isNotEmpty()) {
                    adapterBeneficiary.updateClientDetails(combinedData)
                } else {
                    Log.d("Fragment", "No data to display")
                }
            }
            result.onFailure { exception ->
                Log.e("Fragment", "Error receiving combined data: ${exception.message}")
            }
        }

        // Ensuring user is logged in and fetching data
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            consultationViewModel.loadCombinedData(userId)
        } ?: Log.e("MesBeneficiaires", "User not logged in")
    }
}

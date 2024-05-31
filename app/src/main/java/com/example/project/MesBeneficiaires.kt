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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.MesBenefAdapter
import com.example.project.databinding.MesBeneficiairesBinding
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.MesBeneficiairesViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MesBeneficiaires : Fragment() {
    private val consultationViewModel: ConsultationViewModel by activityViewModels()
    private var _binding: MesBeneficiairesBinding? = null
    private val mesBeneficiairesViewModel: MesBeneficiairesViewModel by viewModels()
    private val binding get() = _binding!!
    private lateinit var adapterBeneficiary: MesBenefAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MesBeneficiairesBinding.inflate(inflater, container, false)
        adapterBeneficiary = MesBenefAdapter(emptyList(), mesBeneficiairesViewModel)
        binding.recyclerViewBeneficiaries.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewBeneficiaries.adapter = adapterBeneficiary
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        mesBeneficiairesViewModel.combinedDataLiveData.observe(viewLifecycleOwner) { result ->
            Log.d("MesBenefViewModel", "Received result from repository: $result")

            result.onSuccess { combinedData ->
                (binding.recyclerViewBeneficiaries.adapter as? MesBenefAdapter)?.updateClientDetails(
                    combinedData
                )
            }
            result.onFailure { exception ->
                Toast.makeText(
                    context,
                    "Error loading data: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            mesBeneficiairesViewModel.loadCombinedData(userId)
        }

    }
}

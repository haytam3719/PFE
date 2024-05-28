package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.ContratBinding
import com.example.project.viewmodels.CollectInfoViewModel
import com.example.project.viewmodels.ContratViewModel
import com.google.android.material.appbar.MaterialToolbar

class Contrat : Fragment() {
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private val contratViewModel: ContratViewModel by viewModels()
    private var numCin:String=""


    private var _binding: ContratBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ContratBinding.inflate(inflater, container, false)
        val view = binding.root


        // Handle clear button click
        binding.reessa.setOnClickListener {
            binding.signaturePad.clear()
        }

        // Handle save button click
        binding.valider.setOnClickListener {
            val signatureBitmap = binding.signaturePad.signatureBitmap

            if (signatureBitmap != null) {

                collectInfoViewModel.observeClient().observe(viewLifecycleOwner, Observer { client ->

                    numCin = client.numCin
                    Log.e("Signature","Catched")
                    contratViewModel.uploadSignatureImage(numCin,signatureBitmap)
                    findNavController().navigate(R.id.contrat_to_mail)

                })
            } else {

            }
        }

        return view
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

        override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setDataToViewModel(data: String) {
        contratViewModel.setData(data)
    }
}

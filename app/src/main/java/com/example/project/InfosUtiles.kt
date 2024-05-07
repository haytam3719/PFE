package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.InfoUtilesBinding

class InfosUtiles : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = InfoUtilesBinding.inflate(inflater, container, false)
        binding.infosUtiles = this
        return binding.root
    }

    fun onClickProtectionDonnees(view:View){
        findNavController().navigate(R.id.infosUtiles_to_protectionDonnes)
    }

    fun onClickGuideSecurite(view:View){
        findNavController().navigate(R.id.infosUtiles_to_guideSecurite)
    }

    fun onClickMentionsLegales(view:View){
        findNavController().navigate(R.id.infosUtiles_to_mentionsLegales)
    }

    fun onClickAgences(view:View){
        findNavController().navigate(R.id.infosUtiles_to_agences)
    }
}
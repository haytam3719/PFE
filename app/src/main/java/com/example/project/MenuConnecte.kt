package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.MenuConnecteBinding

class MenuConnecte : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MenuConnecteBinding.inflate(inflater, container, false)
        binding.menuConnecte = this
        return binding.root
    }

    fun onClickTransferts(view: View){
        findNavController().navigate(R.id.menuConnecte_to_transfers)
    }

    fun onClickPaiements(view: View){
        findNavController().navigate(R.id.menuConnecte_to_paiements)
    }

    fun onClickConsultation(view: View){
        findNavController().navigate(R.id.menuConnecte_to_consultation)
    }

    fun onClickAttijariSecure(view:View){
        findNavController().navigate(R.id.menuConnecte_to_attijariSecure)
    }

    fun onClickAssistanceReclamation(view: View){
        findNavController().navigate(R.id.menuConnecte_to_assistanceReclamation)
    }

    fun onClickInfosUtiles(view: View){
        findNavController().navigate(R.id.menuConnecte_to_infosUtiles)
    }

    fun onClickParametres(view: View){
        findNavController().navigate(R.id.menuConnecte_to_parametres)
    }

    fun onClickButtonCards(view: View){
        findNavController().navigate(R.id.menuConnecte_to_mesCartes)
    }
}
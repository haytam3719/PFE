package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.MenuNonConnecteBinding

class MenuDeconnecte : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MenuNonConnecteBinding.inflate(inflater, container, false)
        binding.menuDeconnecte = this
        return binding.root
    }

    fun onClickReclamation(view: View){
        findNavController().navigate(R.id.menuDeconnecte_to_reclamation)
    }

    fun onClickInfoUtiles(view: View){
        findNavController().navigate(R.id.menuDeconnecte_to_infoUtiles)
    }

    fun onClickParametres(view: View){
        findNavController().navigate(R.id.menuDeconnecte_to_parametres)
    }
}
package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.VirementBinding
import com.example.project.viewmodels.VirementViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Virement : Fragment(){
    //private var virement = Virement("",0.0, "", "", CompteImpl("","", TypeCompte.COURANT,0.0,mutableListOf(),null), CompteImpl("","", TypeCompte.COURANT,0.0,mutableListOf(),null), "","",0.0,"")
    private val virementViewModel:VirementViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        virementViewModel.virementLiveData.observe(viewLifecycleOwner) { virement ->

            Log.d("Virement Details", "Émetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}, Montant: ${virement.montant}")
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = VirementBinding.inflate(inflater, container, false)
        binding.virementViewModel = virementViewModel

        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root

    }


}



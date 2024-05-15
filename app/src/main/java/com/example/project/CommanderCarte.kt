package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.CommanderCarteBinding
import com.example.project.viewmodels.CardsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommanderCarte : Fragment() {

    private val cardsViewModel: CardsViewModel by viewModels()
    private var _binding: CommanderCarteBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CommanderCarteBinding.inflate(inflater, container, false)
        binding.commanderCarte = this

        binding.cardTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if(selected == "Carte Débit"){
                    binding.numCompte.visibility = View.VISIBLE
                }else{
                    binding.numCompte.visibility = View.GONE
                }
                Toast.makeText(requireContext(), "Selected: $selected", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        cardsViewModel.operationStatus.observe(viewLifecycleOwner) { isSuccess ->
            Log.d("CommanderCarte", "Operation status observed: $isSuccess")
            if (isSuccess) {
                Toast.makeText(requireContext(), "Card added successfully", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "Failed to add card", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }


    fun onClickCommanderCarte(view:View){
        Log.d("CommanderCarte", "Button clicked to add a card.")
        val nomTitulaire = binding.nameInputLayout.editText!!.text.toString()
        val adresseFacturation = binding.addressInputLayout.editText!!.text.toString()
        val numCompte = binding.numCompte.editText!!.text.toString()



        val selectedType = binding.cardTypeSpinner.selectedItem.toString()
        if (selectedType == "Carte Débit") {
            val carteDebit = cardsViewModel.generateDebitCard(nomTitulaire,adresseFacturation,numCompte)
            cardsViewModel.ajouterCarte(carteDebit)
        }else{
            val carteCredit = cardsViewModel.generateCreditCard(nomTitulaire,adresseFacturation)
            cardsViewModel.ajouterCarte(carteCredit)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
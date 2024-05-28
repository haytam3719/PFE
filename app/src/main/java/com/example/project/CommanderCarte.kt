package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.AccountData
import com.example.project.databinding.CommanderCarteBinding
import com.example.project.viewmodels.CardsViewModel
import com.example.project.viewmodels.ConsultationViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommanderCarte : Fragment() {

    private val cardsViewModel: CardsViewModel by viewModels()
    private val consultationViewModel: ConsultationViewModel by activityViewModels()
    private lateinit var adapter: AccountsAdapter

    private var _binding: CommanderCarteBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CommanderCarteBinding.inflate(inflater, container, false)
        binding.commanderCarte = this
        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        setupClickListeners()
        adapter = AccountsAdapter { account ->
            consultationViewModel.selectAccount(account)
        }

        binding.listViewOptions.adapter = adapter
        binding.listViewOptions.layoutManager = LinearLayoutManager(context)

        observeAccounts()
        consultationViewModel.fetchAccountsForCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)



        binding.cardTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if(selected == "Carte Débit"){
                    binding.numCompte.visibility = View.VISIBLE
                }else{
                    binding.numCompte.visibility = View.GONE
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        cardsViewModel.operationStatus.observe(viewLifecycleOwner) { isSuccess ->
            Log.d("CommanderCarte", "Operation status observed: $isSuccess")
            if (isSuccess) {
                Toast.makeText(requireContext(), "Carte commandée avec succès", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(requireContext(), "La carte n'a pas été ajoutée. Veuillez rééssayer plus tard", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }







    fun onClickCommanderCarte(view:View){
        Log.d("CommanderCarte", "Button clicked to add a card.")
        val nomTitulaire = binding.nameInputLayout.editText!!.text.toString()
        val adresseFacturation = binding.addressInputLayout.editText!!.text.toString()
        val numCompte = extractAccountNumber(binding.textViewSubtext.text.toString())
        Log.d("Commander Carte",numCompte!!)


        val selectedType = binding.cardTypeSpinner.selectedItem.toString()
        if (selectedType == "Carte Débit") {
            val carteDebit = cardsViewModel.generateDebitCard(nomTitulaire,adresseFacturation,numCompte)
            cardsViewModel.ajouterCarte(carteDebit)
        }else{
            val carteCredit = cardsViewModel.generateCreditCard(nomTitulaire,adresseFacturation)
            cardsViewModel.ajouterCarte(carteCredit)
        }


    }



    private fun observeAccounts() {
        consultationViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            adapter.updateAccounts(accounts)
        }
    }
    private fun setupClickListeners() {
        binding.numCompte.setOnClickListener {
            binding.popupView.visibility = View.VISIBLE
        }


        binding.closeImageView.setOnClickListener {
            binding.popupView.visibility = View.GONE
        }

    }


    private fun updateSelectedAccountUI(account: AccountData) {
        binding.numCompte.apply {
            binding.textViewCompte.text = formatAccountType(account.accountType)
            binding.textViewSubtext.text = "Numéro de compte: ${account.accountNumber}"
            binding.hiddenTv.text = "Solde: ${account.balance} DH"
            binding.endImageView.visibility = View.GONE
        }
        binding.popupView.visibility = View.GONE

    }


    private fun formatAccountType(type: String): String {
        return when (type) {
            "CHEQUES" -> "Compte chèque"
            "COURANT" -> "Compte courant"
            "EPARGNE" -> "Compte épargne"
            else -> type // Default case to handle unexpected types
        }
    }



    private fun observeViewModel() {
        consultationViewModel.selectedAccount.observe(viewLifecycleOwner) { selectedAccount ->
            updateSelectedAccountUI(selectedAccount)
        }
    }



    fun extractAccountNumber(input: String): String? {
        val pattern = "Numéro de compte: (\\w+)"
        val regex = Regex(pattern)
        val matchResult = regex.find(input)
        return matchResult?.groups?.get(1)?.value
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
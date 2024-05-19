package com.example.project

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.AccountData
import com.example.project.adapters.ClientAdapter
import com.example.project.databinding.VirementStepOneBinding
import com.example.project.models.CircularProgressView
import com.example.project.models.ClientAccountDetails
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.ProgressBarViewModel
import com.example.project.viewmodels.VirementUpdatedViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VirementStepOne : Fragment() {

    private val progressViewModel: ProgressBarViewModel by activityViewModels()
    private val consultationViewModel: ConsultationViewModel by viewModels()
    private val sharedViewModel:VirementUpdatedViewModel  by activityViewModels()
    private var _binding: VirementStepOneBinding? = null
    private lateinit var adapter: AccountsAdapter
    private lateinit var adapterBeneficiary: ClientAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VirementStepOneBinding.inflate(inflater, container, false)
        setupClickListeners()
        adapter = AccountsAdapter { account ->
            consultationViewModel.selectAccount(account)
        }

        binding.listViewOptions.adapter = adapter
        binding.listViewOptions.layoutManager = LinearLayoutManager(context)

        observeAccounts()
        consultationViewModel.fetchAccountsForCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val circularProgressBar = binding.circularProgressBar

        if (savedInstanceState == null) {
            progressViewModel.resetProgress()
            Handler(Looper.getMainLooper()).postDelayed({
                animateProgress(circularProgressBar, 25) {
                    progressViewModel.setProgress(25)
                }
            }, 500)
        }

            binding.nextButton.setOnClickListener {
                val accountNumber = binding.textViewSubtext.text.toString().substringAfter("Numéro de compte: ")
                val accountBalance = binding.hiddenTv.text.toString().substringAfter("Solde: ").substringBefore(" DH").toDoubleOrNull() ?: 0.0
                val accountData = AccountData(binding.textViewCompte.text.toString(), accountNumber, accountBalance.toString())
                sharedViewModel.selectAccount(accountData)

                val clientDetails = ClientAccountDetails(
                    binding.textViewBenef.text.toString(),
                    null,
                    null,
                    binding.textViewSubtextBenef.text.toString()
                )
                sharedViewModel.selectClient(clientDetails)

            findNavController().navigate(R.id.action_virementStepOne_to_virementStepTwo)
        }


        observeViewModel()


        adapterBeneficiary = ClientAdapter(emptyList(),consultationViewModel)

        binding.listViewOptionsBenef.layoutManager = LinearLayoutManager(context)
        binding.listViewOptionsBenef.adapter = adapterBeneficiary

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

        consultationViewModel.selectedClient.observe(viewLifecycleOwner) { client ->
            binding.textViewBenef.text = "${client.nom.toUpperCase()} ${client.prenom}"
            binding.textViewSubtextBenef.text = client.accountNumber
            binding.popupViewBenef.visibility = View.GONE
            binding.endImageViewBenef.visibility = View.GONE
            binding.nextButton.visibility = View.VISIBLE
        }
    }





    private fun observeAccounts() {
        consultationViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            adapter.updateAccounts(accounts)
        }
    }

    private fun animateProgress(progressView: CircularProgressView, toProgress: Int, onEnd: () -> Unit) {
        val animator = ValueAnimator.ofInt(progressView.progress, toProgress)
        animator.duration = 500 // 0.5 second for smooth animation
        animator.addUpdateListener { animation ->
            progressView.updateProgress(animation.animatedValue as Int)
        }
        animator.addListener(onEnd = { onEnd() })
        animator.start()
    }



    private fun setupClickListeners() {
        binding.compte.setOnClickListener {
            binding.nextButton.visibility = View.GONE
            binding.popupView.visibility = View.VISIBLE
            binding.beneficiaire.visibility = View.GONE
        }

        binding.beneficiaire.setOnClickListener {
            binding.nextButton.visibility = View.GONE
            binding.popupViewBenef.visibility = View.VISIBLE
        }

        binding.closeImageView.setOnClickListener {
            binding.nextButton.visibility = View.VISIBLE
            binding.popupView.visibility = View.GONE
            binding.beneficiaire.visibility = View.VISIBLE

        }

        binding.closeImageViewBenef.setOnClickListener {
            binding.nextButton.visibility = View.VISIBLE
            binding.popupViewBenef.visibility = View.GONE
        }


    }


    private fun updateSelectedAccountUI(account: AccountData) {
        binding.compte.apply {
            binding.textViewCompte.text = formatAccountType(account.accountType)
            binding.textViewSubtext.text = "Numéro de compte: ${account.accountNumber}"
            binding.hiddenTv.text = "Solde: ${account.balance} DH"
            binding.endImageView.visibility = View.GONE
        }
        binding.popupView.visibility = View.GONE
        binding.beneficiaire.visibility = View.VISIBLE
        binding.nextButton.visibility = View.VISIBLE

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




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

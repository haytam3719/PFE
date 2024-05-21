package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.AccountData
import com.example.project.databinding.RechargeSimple3Binding
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.RechargeViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Recharge3 : Fragment() {

    private var _binding: RechargeSimple3Binding? = null
    private val binding get() = _binding!!

    private val consultationViewModel: ConsultationViewModel by viewModels()
    private val rechargeViewModel: RechargeViewModel by activityViewModels()
    private lateinit var adapter: AccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RechargeSimple3Binding.inflate(inflater, container, false)
        Log.d("Recharge3", "onCreateView: View created")

        setupClickListeners()
        adapter = AccountsAdapter { account ->
            consultationViewModel.selectAccount(account)
        }

        binding.listViewOptions.adapter = adapter
        binding.listViewOptions.layoutManager = LinearLayoutManager(context)

        observeAccounts()
        consultationViewModel.fetchAccountsForCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)

        observeViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Recharge3", "onViewCreated: View created")

        rechargeViewModel.phoneNumber.observe(viewLifecycleOwner) { phoneNumber ->
            Log.d("Recharge3", "Phone number observed: $phoneNumber")
            binding.textInputLayoutPhoneNumber.editText?.setText(phoneNumber)
        }
        rechargeViewModel.montant.observe(viewLifecycleOwner) { montant ->
            Log.d("Recharge3", "Montant observed: $montant")
            binding.tvAmount.text = "Montant sélectionné: $montant"
        }
        rechargeViewModel.rechargeType.observe(viewLifecycleOwner) { rechargeType ->
            Log.d("Recharge3", "Type de Recharge observed: $rechargeType")
            binding.tvType.text = "Type de recharge sélectionné: $rechargeType"
        }
    }

    private fun updateSelectedAccountUI(account: AccountData) {
        binding.clPaymentAccount.apply {
            binding.tvAccountName.text = formatAccountType(account.accountType)
            binding.tvAccountNumber.text = "Numéro de compte: ${account.accountNumber}"
            binding.tvBalance.text = "Solde: ${account.balance} DH"
            binding.ivDropdown.visibility = View.GONE
        }
        binding.popupView.visibility = View.GONE
        binding.buttonPay.visibility = View.VISIBLE
        binding.buttonCancel.visibility = View.VISIBLE
    }

    private fun observeAccounts() {
        consultationViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            adapter.updateAccounts(accounts)
        }
    }

    private fun setupClickListeners() {
        binding.clPaymentAccount.setOnClickListener {
            binding.buttonPay.visibility = View.GONE
            binding.buttonCancel.visibility = View.GONE
            binding.popupView.visibility = View.VISIBLE
        }

        binding.closeImageView.setOnClickListener {
            binding.buttonPay.visibility = View.VISIBLE
            binding.buttonCancel.visibility = View.VISIBLE
            binding.popupView.visibility = View.GONE
        }
    }

    private fun formatAccountType(type: String): String {
        return when (type) {
            "CHEQUES" -> "Compte chèque"
            "COURANT" -> "Compte courant"
            "EPARGNE" -> "Compte épargne"
            else -> type
        }
    }

    private fun observeViewModel() {
        consultationViewModel.selectedAccount.observe(viewLifecycleOwner) { selectedAccount ->
            updateSelectedAccountUI(selectedAccount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Recharge3", "onDestroyView: View destroyed")
        _binding = null
    }
}

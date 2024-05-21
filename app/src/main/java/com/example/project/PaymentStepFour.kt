package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.AccountData
import com.example.project.adapters.BillAdapter2
import com.example.project.databinding.Payment4Binding
import com.example.project.models.Bill
import com.example.project.viewmodels.ConsultationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStepFour : Fragment() {
    private val consultationViewModel: ConsultationViewModel by viewModels()
    private lateinit var adapter: AccountsAdapter

    private var _binding: Payment4Binding? = null
    private val binding get() = _binding!!

    private lateinit var selectedBillIds: List<String>
    private lateinit var selectedBillAmounts: DoubleArray
    private lateinit var selectedBillDueDates: List<String>
    private lateinit var totalApayer:String
    private lateinit var billAdapter: BillAdapter2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Payment4Binding.inflate(inflater, container, false)

        setupClickListeners()
        adapter = AccountsAdapter { account ->
            consultationViewModel.selectAccount(account)
        }

        binding.listViewOptions.adapter = adapter
        binding.listViewOptions.layoutManager = LinearLayoutManager(context)

        observeAccounts()
        consultationViewModel.fetchAccountsForCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)

        observeViewModel()

        arguments?.let {
            selectedBillIds = it.getStringArray("numbers") ?.toList()!!
            selectedBillAmounts = it.getDoubleArray("amounts") ?: doubleArrayOf()
            selectedBillDueDates = it.getStringArray("dueDates")?.toList()!!
            totalApayer = it.getString("totalApayer").toString()

            binding.tvTotalAmount.text = totalApayer.toString()

            Log.d("selectedBillIds", selectedBillIds.toString())
            Log.d("selectedBillAmounts", selectedBillAmounts.contentToString())
            Log.d("selectedBillDueDates", selectedBillDueDates.toString())

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

    }


    private fun setupRecyclerView() {
        val bills = selectedBillIds.mapIndexed { index, id ->
            Bill(id, selectedBillAmounts[index],selectedBillDueDates[index], selectedBillIds[index])
        }

        billAdapter = BillAdapter2(bills)
        binding.recyclerViewBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = billAdapter
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

}
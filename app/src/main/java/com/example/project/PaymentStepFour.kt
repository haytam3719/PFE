package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.databinding.Payment4Binding
import com.example.project.viewmodels.ConsultationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStepFour : Fragment() {
    private val consultationViewModel: ConsultationViewModel by viewModels()
    private lateinit var adapter: AccountsAdapter

    private var _binding: Payment4Binding? = null
    private val binding get() = _binding!!

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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
}
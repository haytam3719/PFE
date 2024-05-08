package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.project.adapters.AccountData
import com.example.project.adapters.CarouselAdapter
import com.example.project.adapters.CarouselPageTransformer
import com.example.project.adapters.TransactionAdapter
import com.example.project.databinding.ConsultationBinding
import com.example.project.prototype.Transaction
import com.example.project.viewmodels.ConsultationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Consultation : Fragment() {

    private val consultationViewModel: ConsultationViewModel by viewModels()
    private lateinit var binding: ConsultationBinding
    private val iconList = listOf(R.drawable.note, R.drawable.note, R.drawable.note, R.drawable.note)
    private lateinit var adapter: TransactionAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ConsultationBinding.inflate(inflater, container, false).apply {
            consultation = this@Consultation
            viewPager.apply {
                adapter = CarouselAdapter(emptyList(), 0, iconList, object : CarouselAdapter.OnCarouselItemClickListener {
                    override fun onItemClick(accountData: AccountData) {
                        val bundle = bundleOf(
                            "accountType" to accountData.accountType,
                            "accountNumber" to accountData.accountNumber,
                            "balance" to accountData.balance
                        )
                        findNavController().navigate(R.id.consultation_to_detailCompte, bundle)
                    }
                })

                val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
                val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)
                val increasedOffsetPx = resources.getDimensionPixelOffset(R.dimen.increased_offset)
                setPadding(increasedOffsetPx, 0, increasedOffsetPx, 0)
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 3

                setPageTransformer(CarouselPageTransformer())
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        (adapter as? CarouselAdapter)?.currentPagePosition = position
                        adapter?.notifyDataSetChanged()
                    }
                })
            }
        }

        consultationViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            if (accounts.isNotEmpty()) {
                updateAccountsDisplay(accounts)
                Log.d("ConsultationFragment", "Accounts updated in UI: $accounts")
            } else {
                Log.d("ConsultationFragment", "No accounts to display")
            }
        }

        FirebaseAuth.getInstance().currentUser?.let {
            consultationViewModel.fetchAccountsForCurrentUser(
                it.uid)
        }

        return binding.root
    }


    private fun updateAccountsDisplay(accounts: List<AccountData>) {
        (binding.viewPager.adapter as? CarouselAdapter)?.setData(accounts)
    }

    fun onClickAddAccount(view: View) {
        findNavController().navigate(R.id.consultation_to_createaccount)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        adapter = TransactionAdapter(emptyList(), FirebaseAuth.getInstance().currentUser!!.uid, object : TransactionAdapter.OnRecycleViewItemClickListener {

            override fun onItemClick(transactionData: Transaction) {
                val bundle = bundleOf(
                    "transactionId" to transactionData.idTran
                )
                findNavController().navigate(R.id.consultation_to_detailTransaction, bundle)
            }
        })
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvRecentTransactions.adapter = adapter
    }




    private fun setupViewModel() {
        observeTransactions()
        consultationViewModel.loadTransactions(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private fun observeTransactions() {
        consultationViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }
    }



}

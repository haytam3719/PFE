package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.example.project.viewmodels.DashboardViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Consultation : Fragment() {

    private val consultationViewModel: ConsultationViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private lateinit var binding: ConsultationBinding
    private val iconList = listOf(R.drawable.note, R.drawable.note, R.drawable.note, R.drawable.note)
    private lateinit var adapter: TransactionAdapter
    private lateinit var webView: WebView


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
                setPadding(50, 10, 50, 20)
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
            if(accounts.isNullOrEmpty()){
                binding.tvComptes.visibility = View.VISIBLE
                binding.quotasComptes.visibility = View.GONE
                binding.webView.visibility = View.GONE
                binding.textDernieresOperations.visibility = View.GONE
            }
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

        consultationViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        dashboardViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })



        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
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

        webView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.textSize = WebSettings.TextSize.LARGER


        dashboardViewModel.accountBalances.observe(viewLifecycleOwner) { balances ->
            if (balances.isNotEmpty()) {
                updateWebView(balances)
            }
        }

        dashboardViewModel.loadAccountBalances(FirebaseAuth.getInstance().currentUser!!.uid)
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


    private fun updateWebView(balances: List<Pair<String, Double>>) {
        val dataString = balances.joinToString(",") { "['${it.first}', ${it.second}]" }
        val htmlContent = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
            body, html {
                margin: 0;
                padding: 0;
                height: 100%;
                width: 100%;
                font-family: 'Arial', sans-serif;
            }
            #piechart {
                height: 100%;
                width: 100%;
                display: flex;
                justify-content: center;
                align-items: center;
            }
        </style>
        <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
        <script type="text/javascript">
            google.charts.load('current', {'packages':['corechart']});
            google.charts.setOnLoadCallback(drawChart);
            function drawChart() {
                var data = google.visualization.arrayToDataTable([
                    ['Category', 'Amount'],
                    $dataString
                ]);

                var options = {
                    title: 'Account Balances by Category',
                    pieHole: 0.4,
                    colors: ['#FF9800', '#FF5722', '#FFC107', '#FFEB3B', '#795548'],
                    backgroundColor: 'transparent',
                    chartArea: {left: 0, top: 0, width: '100%', height: '100%'},
                    titleTextStyle: { fontSize: 20, bold: true },
                    legend: { 
                        position: 'bottom',
                        textStyle: { fontSize: 18 }
                    },
                    tooltip: { 
                        textStyle: { fontSize: 16 },
                        showColorCode: true
                    },
                    animation: {
                        startup: true,
                        duration: 5000,
                        easing: 'linear'
                    },
                };

                var chart = new google.visualization.PieChart(document.getElementById('piechart'));
                chart.draw(data, options);
            }
        </script>
    </head>
    <body>
        <div id="piechart"></div>
    </body>
    </html>
    """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }




}

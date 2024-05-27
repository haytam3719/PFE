package com.example.project

import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.TransactionAdapter
import com.example.project.databinding.DashboardBinding
import com.example.project.models.DeviceInfo
import com.example.project.models.EmailRequest
import com.example.project.models.EmailResponse
import com.example.project.models.MailApiClient
import com.example.project.models.MailApiService
import com.example.project.prototype.Transaction
import com.example.project.viewmodels.AuthViewModel
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@AndroidEntryPoint
class Dashboard : Fragment(){
    private val dashboardViewModel by viewModels<DashboardViewModel>()
    private lateinit var deviceInfo: DeviceInfo
    private val authViewModel:AuthViewModel by viewModels()
    private val consultationViewModel:ConsultationViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private var _binding: DashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var webView: WebView



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.navigateToPayment.observe(
            viewLifecycleOwner,
            Observer{shouldnavigate ->
                if(shouldnavigate){
                    findNavController().navigate(R.id.dashboard_to_payment)

                    dashboardViewModel.onNavigationCompletePayment()
                }
            }
        )

        dashboardViewModel.navigateToVirement.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(R.id.dashboard_to_virement)

                    // Reset the navigation event after navigation
                    dashboardViewModel.onNavigationComplete()
                }
            })


        dashboardViewModel.navigateToCreateAccount.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    findNavController().navigate(R.id.dashboard_to_createaccount)

                    dashboardViewModel.onNavigationCompleteCreateAccount()
                }
            })


        deviceInfo = DeviceInfo.fromContext(requireContext())
        dashboardViewModel.deviceInfoList.observe(viewLifecycleOwner, Observer { deviceList ->
            if (deviceList != null) {
                Log.d("Fetched devices list", deviceList.toString())
                val connectedDevice = dashboardViewModel.getDeviceInfo(requireContext())

                val isTrustedDevice = deviceList.any { deviceInfo ->
                    deviceInfo.deviceModel == connectedDevice.deviceModel &&
                            deviceInfo.androidVersion == connectedDevice.androidVersion &&
                            deviceInfo.networkOperatorName == connectedDevice.networkOperatorName
                }

                if (isTrustedDevice) {
                    Log.d("Connected Device", "Trusted Device")
                    binding.contentLayout.visibility = View.VISIBLE

                } else {
                    Log.e("Connected Device", "Non-trusted Device")
                    showSuspiciousConnectionPopup()
                    val currentUseruid = FirebaseAuth.getInstance().currentUser!!.uid
                    viewLifecycleOwner.lifecycleScope.launch{
                        val currentClient = dashboardViewModel.fetchClientDetails(currentUseruid)
                        if (currentClient != null) {
                            sendSMS("0${currentClient.numTele}","Nous avons détecté une connexion suspecte. S'agissait bien de vous?")
                            FirebaseAuth.getInstance().currentUser!!.email?.let {
                                sendSuspectConnectionEmail(
                                    it,"Attention: Connexion suspecte détectée sur votre compte","Version d'Android: ${dashboardViewModel.getDeviceInfo(requireContext()).androidVersion},\nModèle de l'appareil: ${dashboardViewModel.getDeviceInfo(requireContext()).deviceModel},\nOpérateur: ${dashboardViewModel.getDeviceInfo(requireContext()).networkOperatorName},\nAdresse IP: ${dashboardViewModel.getDeviceInfo(requireContext()).ipAddress}")
                            }
                        }

                    }
                    binding.contentLayout.visibility = View.VISIBLE
                }
            } else {
                Log.d("Dashboard", "Device list is null or empty")
            }
        })

        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        dashboardViewModel.fetchDeviceListByClientUid(userUid)


        dashboardViewModel.totalBalance.observe(viewLifecycleOwner, Observer { totalBalance ->
            binding.tvTotalBalance.text = "$totalBalance DH"
        })

        dashboardViewModel.loadTotalBalance(userUid)


    }


    fun showSuspiciousConnectionPopup() {
        val context = requireContext()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Connexion suspecte")
        builder.setMessage("L'appareil actuel ne peut pas être trouvé dans la liste des appareils de confiance associés à l'utilisateur.")
        builder.setCancelable(false)

        builder.setPositiveButton("Faire confiance") { dialog, which ->
            val bundle = Bundle().apply {
                putBoolean("shouldTrustDevice", true)
            }
            findNavController().navigate(R.id.dashboard_to_verifyCredentials,bundle)
        }

        builder.setNegativeButton("Se déconnecter") { dialog, which ->
            authViewModel.signOut()
            findNavController().navigate(R.id.dashboard_to_mainactplaceholder)
        }

        builder.create().show()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardBinding.inflate(inflater, container, false)

        binding.contentLayout.visibility = View.VISIBLE
        binding.dashboard = this
        setupViewModel()
        adapter = TransactionAdapter(emptyList(), FirebaseAuth.getInstance().currentUser!!.uid, object : TransactionAdapter.OnRecycleViewItemClickListener {

            override fun onItemClick(transactionData: Transaction) {
                val bundle = bundleOf(
                    "transactionId" to transactionData.idTran
                )
                findNavController().navigate(R.id.dashboard_to_detailTransaction, bundle)
            }
        })
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(context)
        binding.rvRecentTransactions.adapter = adapter


        webView = binding.graphPlaceholder
        webView.settings.javaScriptEnabled = true

        dashboardViewModel.balanceOverTime.observe(viewLifecycleOwner, Observer { balances ->
            if (balances.isNotEmpty()) {
                updateWebView(balances)
            }
        })

        dashboardViewModel.loadBalanceOverTime(FirebaseAuth.getInstance().currentUser!!.uid)

        return binding.root

    }


    private fun setupViewModel() {
        observeTransactions()
        consultationViewModel.loadTransactions(FirebaseAuth.getInstance().currentUser?.uid ?: "")
    }

    private fun observeTransactions() {
        consultationViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.updateTransactions(transactions)
        }
    }

    //onClicks

    fun onClickMesComptes(view:View){
        findNavController().navigate(R.id.dashboard_to_consultation)
    }

    fun onClickAttijariSecure(view:View){
        findNavController().navigate(R.id.dashboard_to_attijariSecure)
    }

    fun onClickSettings(view:View){
        findNavController().navigate(R.id.dashboard_to_parametres)
    }


    private fun updateWebView(balances: List<Pair<String, Double>>) {
        val dataString = balances.joinToString(",") { "['${it.first}', ${it.second}]" }
        val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <link href="https://fonts.googleapis.com/css?family=Roboto:300,400,500" rel="stylesheet">
            <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
            <script type="text/javascript">
                google.charts.load('current', {'packages':['corechart']});
                google.charts.setOnLoadCallback(drawChart);
                function drawChart() {
                    var data = google.visualization.arrayToDataTable([
                        ['Date', 'Solde (MAD)'],
                        $dataString
                    ]);
                    
                    var options = {
                        title: 'Evolution of Balance',
                        titleTextStyle: {
                            color: '#333', 
                            fontSize: 20, 
                            bold: true,
                            fontFamily: 'Roboto'
                        },
                        hAxis: {
                            title: 'Date',
                            titleTextStyle: {color: '#888', fontSize: 14, italic: true},
                            textStyle: {color: '#555', fontSize: 12, fontFamily: 'Roboto'},
                            showTextEvery: 1
                        },
                        vAxis: {
                            title: 'Solde (MAD)',
                            textStyle: {color: '#555', fontSize: 12, fontFamily: 'Roboto'},
                            titleTextStyle: {color: '#888', fontSize: 14, italic: true},
                            format: '#,##0 MAD',
                            gridlines: {color: '#e0e0e0', count: 4}
                        },
                        legend: { position: 'none' },
                        colors: ['#ff7043'],
                        backgroundColor: '#fafafa',
                        chartArea: {
                            backgroundColor: {
                                fill: '#fafafa',
                                fillOpacity: 0.8
                            },
                            left: '25%', top: '10%', width: '100%', height: '75%'
                        },
                        animation: {
                            startup: true,
                            duration: 1500,
                            easing: 'out'
                        },
                        lineWidth: 3,
                        areaOpacity: 0.2,
                        pointsVisible: true
                    };

                    var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
                    chart.draw(data, options);
                }
            </script>
        </head>
        <body>
            <div id="chart_div" style="width: 100%; height: 100%;"></div>
        </body>
        </html>
    """.trimIndent()

        webView?.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }


    fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            println("SMS sent successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to send SMS: ${e.message}")
        }
    }






    //Fraud Email : Suspect Connection

    private fun sendSuspectConnectionEmail(toEmail: String, subject: String, deviceInfos: String) {
        val apiService = MailApiClient.retrofit.create(MailApiService::class.java)
        val imageUrl = "https://historiadelaempresa.com/wp-content/uploads/logotipo/Attijariwafa-Bank.png"

        val emailContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Connexion suspecte détectée</title>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    line-height: 1.6;
                    margin: 0;
                    padding: 20px;
                    background-color: #f9f9f9;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    padding: 20px;
                    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                    border-radius: 8px;
                }
                .title {
                    font-size: 24px;
                    font-weight: bold;
                    color: #333333;
                    margin-bottom: 10px;
                }
                .content {
                    font-size: 16px;
                    color: #555555;
                    margin-bottom: 20px;
                }
                .footer img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                    margin: 0 auto;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="title">Connexion suspecte détectée</div>
                <div class="content">
                    <p>Notre système a détecté une nouvelle connexion suspecte depuis un appareil inconnu. Voici les informations de l'appareil :</p>
                    <ul>
                        <li>$deviceInfos</li>
                    </ul>
                    <p>Si cette connexion ne vous semble pas familière, veuillez changer votre mot de passe immédiatement et contacter notre service clientèle.</p>
                    <p>Nous vous remercions de votre vigilance et restons à votre disposition pour toute question ou assistance.</p>
                </div>
                <div class="footer">
                    <img src="$imageUrl" alt="Footer Image">
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

        val request = EmailRequest(email = toEmail, subject = subject, content = emailContent)

        CoroutineScope(Dispatchers.IO).launch {
            apiService.sendEmail(request).enqueue(object : Callback<EmailResponse> {
                override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Un mail a été envoyé à l'email d'origine", Toast.LENGTH_LONG).show()
                        } else {
                            Log.e("MailFragment", "Failed to send email: ${response.message()}")
                            Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.e("MailFragment", "Network error: ${t.message}", t)
                        Toast.makeText(context, "Erreur de connexion: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
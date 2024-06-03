package com.example.project

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.FragmentOtpBinding
import com.example.project.models.AccountCreationServiceImpl
import com.example.project.models.CompteImpl
import com.example.project.models.DeviceInfo
import com.example.project.models.EmailRequest
import com.example.project.models.EmailResponse
import com.example.project.models.MailApiClient
import com.example.project.models.MailApiService
import com.example.project.models.VirementViewModelFactory
import com.example.project.prototype.AccountRepository
import com.example.project.repositories.AccountRepositoryImpl
import com.example.project.viewmodels.CollectInfoViewModel
import com.example.project.viewmodels.DashboardViewModel
import com.example.project.viewmodels.OtpViewModel
import com.example.project.viewmodels.PaymentFourViewModel
import com.example.project.viewmodels.PaymentViewModel
import com.example.project.viewmodels.PaymentViewModelUpdated
import com.example.project.viewmodels.TransportVirementViewModel
import com.example.project.viewmodels.VirementUpdatedViewModel
import com.example.project.viewmodels.VirementViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class OTPHandler : Fragment() {
    companion object {
        private const val SEND_SMS_PERMISSION_REQUEST_CODE = 123
    }
    private val otpViewModel: OtpViewModel by viewModels()
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private lateinit var deviceInfo: DeviceInfo
    private var actualText= ""
    private val transportVirementViewModel: TransportVirementViewModel by activityViewModels()
    private val paymentViewModelUpdated: PaymentViewModelUpdated by activityViewModels()
    private val paymentViewModel:PaymentViewModel by viewModels()
    private val dashboardViewModel : DashboardViewModel by viewModels()
    private val accountRepository: AccountRepository = AccountRepositoryImpl(
        AccountCreationServiceImpl()
    )

    private var currentVirement = com.example.project.models.Virement()
    private lateinit var virementViewModelFactory: VirementViewModelFactory
    private lateinit var virementViewModel: VirementViewModel

    private var bundle:String = "nothing"
    private var selectedAccountId: String? = null

    private val paymentFourViewModel: PaymentFourViewModel by activityViewModels()
    private val sharedViewModel: VirementUpdatedViewModel by activityViewModels()
    private var fromVirement = false
    private var fromPayment = false
    private var currentNumTel = ""

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    //val initialClientValue = clientViewModel.client.value



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VM_VALUE", "CollectInfosViewModel: ${collectInfoViewModel.clientViewModel}")

        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        otpViewModel.navigateToPrint.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navigate when event is triggered
                findNavController().navigate(R.id.otp_to_print)

                // Reset the navigation event after navigation
                otpViewModel.onNavigationComplete()
            }
        })

        deviceInfo = DeviceInfo.fromContext(requireContext())


        collectInfoViewModel.observeClient().observe(viewLifecycleOwner,  Observer { client ->
            // Handle client update here
            // You can update UI or perform any other actions based on the client data
            // For example, log the client data
            client.deviceInfo=deviceInfo
            client.deviceInfoList = listOf(deviceInfo)
        })


        fromVirement = arguments?.getBoolean("fromVirement") ?: false
        Log.e("Navigation DEBUG", "fromVirement: $fromVirement")


        fromPayment = arguments?.getBoolean("fromPayment") ?: false
        Log.e("Navigation DEBUG", "fromPayment: $fromPayment")

        selectedAccountId = arguments?.getString("selectedAccountId")
        Log.d("Navigation DEBUG", "selectedAccountId: $selectedAccountId")



        if(fromVirement) {
            bundle = "fromVirement"
            otpViewModel.otpBiometricVerified.observe(viewLifecycleOwner, Observer { verified ->
                if (verified) {
                    transportVirementViewModel.virement.observe(
                        viewLifecycleOwner,
                        Observer { virement ->


                            virementViewModelFactory =
                                VirementViewModelFactory(virement, accountRepository)
                            Log.e(
                                "Virement Details from OTPHandler",
                                "Émetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}, Montant: ${virement.montant}"
                            )
                            // Initialize the ViewModel using the factory
                            virementViewModel = ViewModelProvider(
                                this,
                                virementViewModelFactory
                            )[VirementViewModel::class.java]
                            virementViewModel.onButtonClick(view)



                            viewLifecycleOwner.lifecycleScope.launch {
                                try {
                                    // Fetch the recipient and emettor account numbers from the shared ViewModel
                                    val accountNumRecipient = sharedViewModel.selectedClient.value?.accountNumber
                                    val accountNumEmettor = sharedViewModel.selectedAccount.value?.accountNumber

                                    Log.d("Debug", "Fetching account details for recipient: $accountNumRecipient and emettor: $accountNumEmettor")

                                    // Fetch the recipient and emettor account details concurrently
                                    val recipientAccountDeferred = async {
                                        suspendCoroutine<CompteImpl?> { continuation ->
                                            if (accountNumRecipient != null) {
                                                accountRepository.getAccountByNumero(accountNumRecipient) { account ->
                                                    continuation.resume(account)
                                                }
                                            }
                                        }
                                    }

                                    val emettorAccountDeferred = async {
                                        suspendCoroutine<CompteImpl?> { continuation ->
                                            if (accountNumEmettor != null) {
                                                accountRepository.getAccountByNumero(accountNumEmettor) { account ->
                                                    continuation.resume(account)
                                                }
                                            }
                                        }
                                    }

                                    val recipientAccount = recipientAccountDeferred.await()
                                    val emettorAccount = emettorAccountDeferred.await()

                                    if (recipientAccount != null && emettorAccount != null) {
                                        // Use the account's id_proprietaire to get the recipient and emettor UIDs
                                        val recipientUid = recipientAccount.id_proprietaire
                                        val emettorUid = emettorAccount.id_proprietaire

                                        Log.d("Debug", "Fetching client details for recipient UID: $recipientUid and emettor UID: $emettorUid")

                                        // Fetch the recipient and emettor client details concurrently
                                        val recipientClientDeferred = async {
                                            otpViewModel.fetchClientDetails(recipientUid)
                                        }

                                        val emettorClientDeferred = async {
                                            otpViewModel.fetchClientDetails(emettorUid)
                                        }

                                        val recipientClient = recipientClientDeferred.await()
                                        val emettorClient = emettorClientDeferred.await()

                                        if (recipientClient != null && emettorClient != null) {
                                            val formattedCompteEmet = formatAccountNumber(emettorAccount.numero)
                                            val formattedCompteBenef = formatAccountNumber(recipientAccount.numero)
                                            val messageToEmettor = "Votre virement a été effectué avec succès: Compte émetteur: $formattedCompteEmet, Bénéficiaire: M/Mme ${recipientClient.nom.toUpperCase()} ${recipientClient.prenom}, Montant: ${virement.montant} DH, Date: ${virement.date}"
                                            val messageToBenef = "Vous avez reçu un montant de ${virement.montant} DH de la part de ${emettorClient.nom.toUpperCase()} ${emettorClient.prenom}"

                                            Log.d("Debug", "Sending SMS to recipient: 0${recipientClient.numTele} with message: $messageToBenef")
                                            sendSMS("0${recipientClient.numTele}", messageToBenef)
                                            Log.d("Observe Num Tel", "Recipient num: 0${recipientClient.numTele}")

                                            Log.d("Debug", "Sending SMS to emettor: 0${emettorClient.numTele} with message: $messageToEmettor")
                                            sendSMS("0${emettorClient.numTele}", messageToEmettor)
                                            virementViewModel.sendNotificationToRecipient(recipientUid,"Vous avez reçu un montant de ${virement.montant} DH, de la part de ${emettorClient.nom.toUpperCase()} ${emettorClient.prenom} le ${virement.date}")


                                            FirebaseAuth.getInstance().currentUser!!.email?.let {
                                                sendEmailToEmettor(
                                                    it,formattedCompteEmet,"${recipientClient.nom.toUpperCase()} ${recipientClient.prenom}",virement.montant.toString(),virement.date)
                                            }





                                        } else {
                                            throw Exception("Failed to fetch client details for recipient or emettor")
                                        }
                                    } else {
                                        throw Exception("Recipient or emettor account not found")
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to get recipient or emettor details: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val formattedCompteEmet = formatAccountNumber(virement.compteEmet.numero)
                                    val formattedCompteBenef = formatAccountNumber(virement.compteBenef.numero)
                                    val messageToEmettor = "Votre virement a été effectué avec succès: Compte émetteur: $formattedCompteEmet, Bénéficiaire: $formattedCompteBenef, Montant: ${virement.montant} DH, Date: ${virement.date}"
                                    Log.d("SMS", "Sending virement confirmation SMS (failure case): $messageToEmettor")
                                    sendSMS(currentNumTel, messageToEmettor)
                                }
                            }


                            virementViewModel.handleSuccessfulVirement()
                        })
                }
            })
        }


        if(fromPayment) {
            bundle = "fromPayment"

            if(paymentFourViewModel.selectedCard.value != null && selectedAccountId == null){
                if(paymentFourViewModel.selectedCard.value!!.numeroCompte==null){
                    //Carte Crédit
                    Toast.makeText(requireContext(), "Les cartes Crédit ne sont pas encore prise en charge lors des paiements",Toast.LENGTH_LONG).show()
                }else{
                    if(paymentFourViewModel.selectedCard.value!!.loadBlockStatus(requireContext())){
                        //Carte Bloquée
                        Toast.makeText(requireContext(),"La carte est actuellement bloquée. Veuillez changer la méthode de paiement",Toast.LENGTH_LONG).show()
                    }else {
                        Log.d("Card", paymentFourViewModel.selectedCard.value?.numeroCompte.toString())
                        otpViewModel.otpBiometricVerifiedPayment.observe(viewLifecycleOwner) { verified ->
                            if (verified) {
                                paySelectedBillsUsingCard()
                            }
                        }
                    }
            }}

            else{
                otpViewModel.otpBiometricVerifiedPayment.observe(viewLifecycleOwner) { verified ->
                if (verified) {
                    paySelectedBills()

                }
            }
            }
        }

        checkAndRequestPermission()
        binding.verifyButton.setOnClickListener {
            val enteredText = binding.editTextOTP.text.toString()
            otpViewModel.onButtonClickProvisoire(requireActivity(), enteredText,actualText,bundle)

            Log.e("Debug", actualText) // Log the entered text
        }


    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun paySelectedBills() {
        paymentViewModelUpdated.bills.value?.forEach { bill ->
            val billAmount = bill.amount
            Log.d("Bill ID",bill.id)
            Log.d("Bill Amount",billAmount.toString())
            initiatePayment(bill.id, (billAmount+0.25*billAmount).toInt())
            otpViewModel.clientDetails.observe(viewLifecycleOwner){result->
                result.onSuccess { client->
                    val message = "Votre paiement a été effetcué avec succès. Facture N ${bill.id}, Montant: ${(billAmount+0.25*billAmount).toInt()} DH"
                    Log.d("Paiement Phone Num","0${client.numTele}")
                    Log.d("SMS", "Sending payment confirmation SMS: $message")
                    sendSMS("0${client.numTele}",message)
                }

            }


            if (selectedAccountId != null) {
                paymentViewModel.handleSuccessfulPayment("Votre paiement relatif à la facture N° ${bill.id}, montant: ${(billAmount+0.25*billAmount).toInt()} DH a été réglé")
                Log.d("paySelectedBills", "Selected account: $selectedAccountId")
                paymentViewModel.makePaiement((bill.amount+0.25*bill.amount).toDouble(), "paiement", selectedAccountId!!)


            } else {
                Log.e("paySelectedBills", "No account selected")
            }
        }
    }

    private fun initiatePayment(billId: String, amount: Int) {
        val requestData = JSONObject().apply {
            put("bill_id", billId)
            put("amount", amount)
        }

        val jsonStr = Uri.encode(requestData.toString())

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://payment-gatewayapi.onrender.com/initiate_square_oauth?data=$jsonStr")
        }

        startActivity(intent)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun paySelectedBillsUsingCard() {
        paymentViewModelUpdated.bills.value?.forEach { bill ->
            val billAmount = bill.amount
            Log.d("Bill ID",bill.id)
            Log.d("Bill Amount",billAmount.toString())
            initiatePayment(bill.id, (billAmount+0.25*billAmount).toInt())
            otpViewModel.clientDetails.observe(viewLifecycleOwner){result->
                result.onSuccess { client->
                    val message = "Votre paiement a été effetcué avec succès. Facture N ${bill.id}, Montant: ${(billAmount+0.25*billAmount).toInt()} DH"
                    Log.d("Paiement Phone Num","0${client.numTele}")
                    Log.d("SMS", "Sending payment confirmation SMS: $message")
                    sendSMS("0${client.numTele}",message)
                }

            }

            val selectedAccountNum = paymentFourViewModel.selectedCard.value?.numeroCompte
            if (selectedAccountNum != null) {
                paymentViewModel.handleSuccessfulPayment("Votre paiement relatif à la facture N° ${bill.id}, montant: ${(billAmount+0.25*billAmount).toInt()} DH a été réglé")
                Log.d("paySelectedBills", "Selected account (card): $selectedAccountNum")
                paymentViewModel.makePaiementUsingCard((bill.amount+0.25*bill.amount).toDouble(), "Paiement", selectedAccountNum,paymentFourViewModel.selectedCard.value!!)
            } else {
                Log.e("paySelectedBills", "No account selected")
            }
        }
    }


    override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    _binding = FragmentOtpBinding.inflate(inflater, container, false)

    binding.otp = otpViewModel

    binding.lifecycleOwner = viewLifecycleOwner

    return binding.root
}



    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.SEND_SMS),
                SEND_SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            actualText=otpViewModel.generateOTP()
            Log.e("OTP", actualText)
            if (fromVirement || fromPayment) {
                val currentClient = FirebaseAuth.getInstance().currentUser!!.uid
                Log.d("OTPHandler", "Current client UID: $currentClient")

                otpViewModel.clientDetails.observe(viewLifecycleOwner){result->
                    result.onSuccess { client->
                        Log.d("Téléphone Current Client","0${client.numTele}")
                        sendSMS(
                            "0${client.numTele}",
                            "Code de vérification : Veuillez saisir ce code pour pouvoir continuer $actualText"
                        )
                        currentNumTel = "0${client.numTele}"
                    }

                }

                otpViewModel.getClientDetailsByUid(currentClient)





            }else{
                actualText=otpViewModel.generateOTP()
                sendSMS(
                    "0${collectInfoViewModel.tel_portable}",
                    "Code de vérification : Veuillez saisir ce code pour pouvoir continuer $actualText"
                )
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                collectInfoViewModel.observeClient().observe(viewLifecycleOwner){
                    client->
                    actualText=otpViewModel.generateOTP()
                    sendSMS("0${client.numTele}","Code de vérification : Veuillez saisir ce code pour pouvoir continuer $actualText"
                )
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission refusée.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    private fun fetchRecipientDetailsAndSendSMS(virement: com.example.project.models.Virement, currentNumTel: String) {


    }





    private fun formatAccountNumber(accountNumber: String): String {
        return if (accountNumber.startsWith("ACC")) {
            "ACC ******${accountNumber.takeLast(4)}"
        } else {
            accountNumber
        }
    }



    private fun sendEmailToEmettor(email: String, formattedCompteEmet: String, recipientName: String, montant: String, date: String) {
        val apiService = MailApiClient.retrofit.create(MailApiService::class.java)
        val imageUrl = "https://historiadelaempresa.com/wp-content/uploads/logotipo/Attijariwafa-Bank.png"
        val emailSubject = "Confirmation de Virement"

        val emailContent = """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Confirmation de Virement</title>
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
                <div class="title">Confirmation de Virement</div>
                <div class="content">
                    <p>Votre virement a été effectué avec succès:</p>
                    <ul>
                        <li><strong>Compte émetteur :</strong> $formattedCompteEmet</li>
                        <li><strong>Bénéficiaire :</strong> $recipientName</li>
                        <li><strong>Montant :</strong> $montant DH</li>
                        <li><strong>Date :</strong> $date</li>
                    </ul>
                    <p>Nous vous remercions de votre confiance et restons à votre disposition pour toute question ou assistance.</p>
                </div>
                <div class="footer">
                    <img src="$imageUrl" alt="Footer Image">
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

        val request = EmailRequest(email = email, subject = emailSubject, content = emailContent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.sendEmail(request).enqueue(object : Callback<EmailResponse> {
                    override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                        if (response.isSuccessful) {
                            // Email sent successfully
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Email sent successfully", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Erreur de connexion: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }




    private fun sendEmailToBenef(email: String, emettorName: String, montant: String) {
        val apiService = MailApiClient.retrofit.create(MailApiService::class.java)
        val imageUrl = "https://historiadelaempresa.com/wp-content/uploads/logotipo/Attijariwafa-Bank.png"
        val emailSubject = "Notification de Réception de Virement"

        val emailContent = """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Notification de Réception de Virement</title>
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
                <div class="title">Notification de Réception de Virement</div>
                <div class="content">
                    <p>Vous avez reçu un montant de $montant DH de la part de $emettorName.</p>
                    <p>Nous vous remercions de votre confiance et restons à votre disposition pour toute question ou assistance.</p>
                </div>
                <div class="footer">
                    <img src="$imageUrl" alt="Footer Image">
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

        val request = EmailRequest(email = email, subject = emailSubject, content = emailContent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.sendEmail(request).enqueue(object : Callback<EmailResponse> {
                    override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                        if (response.isSuccessful) {
                            // Email sent successfully
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Email sent successfully", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Failed to send email: ${response.message()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Erreur de connexion: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}













    /*override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply{
            setContent(){
                //val navController = rememberNavController()
                //OtpComponent(context = requireActivity(), PERMISSION_REQUEST_SEND_SMS = SEND_SMS_PERMISSION_REQUEST_CODE, navController)
            }

        }}}
    /*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                sendSMS("1234567890", "Your OTP is: 123456")
            } else {
                //
            }
        }
    }
}

@Composable
fun TextField_OTP(label:String):String{
    var text by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { newText->
                        text=newText
        },
        label = { Text(label) }

    )
    return text
}

@Composable
fun OtpComponent(context: Activity,PERMISSION_REQUEST_SEND_SMS:Int,navController: NavController?) {
    val otpText= remember { mutableStateOf("")}
    CenterAlignedTopAppBarExample(text = "Vérification par OTP")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Veuillez entrer le code qui vous est envoyé", Modifier.padding(150.dp))
        val enteredText=TextField_OTP(label = "Code de vérification via OTP")
        otpText.value=enteredText

        Spacer(Modifier.height(15.dp))
        ElevatedButtonExample(text = "Send Message") {

            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                sendSMS("1234567890", "Your OTP is: 123456")
            } else {

                requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.SEND_SMS),
                    PERMISSION_REQUEST_SEND_SMS
                )
            }
        }
        ElevatedButtonExample(text = "Valider") {
            if(enteredText.equals("1234567890")){
                Log.e("CORRECT CODE", "Le code entré est correct")
                if (navController != null) {
                    navController.navigate("checkFingerPrint")
                }

            }
            else{
                Log.e("INCORRECT CODE", "Le code entré est incorrect")
            }
        }
    }
}


    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            // SMS sent successfully
        } catch (e: Exception) {
            // Failed to send SMS
            e.printStackTrace()
        }
    }


*/
*/
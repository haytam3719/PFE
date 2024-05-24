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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.FragmentOtpBinding
import com.example.project.models.AccountCreationServiceImpl
import com.example.project.models.DeviceInfo
import com.example.project.models.VirementViewModelFactory
import com.example.project.prototype.AccountRepository
import com.example.project.repositories.AccountRepositoryImpl
import com.example.project.viewmodels.CollectInfoViewModel
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.OtpViewModel
import com.example.project.viewmodels.PaymentFourViewModel
import com.example.project.viewmodels.PaymentViewModel
import com.example.project.viewmodels.PaymentViewModelUpdated
import com.example.project.viewmodels.TransportVirementViewModel
import com.example.project.viewmodels.VirementViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class OTPHandler : Fragment() {
    companion object {
        private const val SEND_SMS_PERMISSION_REQUEST_CODE = 123
    }
    private val otpViewModel: OtpViewModel by viewModels()
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var actualText:String
    private val transportVirementViewModel: TransportVirementViewModel by activityViewModels()
    private val paymentViewModelUpdated: PaymentViewModelUpdated by activityViewModels()
    private val paymentViewModel:PaymentViewModel by viewModels()
    private val consultationViewModel : ConsultationViewModel by viewModels()
    private val accountRepository: AccountRepository = AccountRepositoryImpl(
        AccountCreationServiceImpl()
    )

    private lateinit var virement: Virement
    private lateinit var virementViewModelFactory: VirementViewModelFactory
    private lateinit var virementViewModel: VirementViewModel

    private var bundle:String = "nothing"
    private var selectedAccountId: String? = null

    private val paymentFourViewModel: PaymentFourViewModel by activityViewModels()

    //val initialClientValue = clientViewModel.client.value



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VM_VALUE", "CollectInfosViewModel: ${collectInfoViewModel.clientViewModel}")
        checkAndRequestPermission()


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
        })


        val fromVirement = arguments?.getBoolean("fromVirement") ?: false
        Log.e("Navigation DEBUG", "fromVirement: $fromVirement")


        val fromPayment = arguments?.getBoolean("fromPayment") ?: false
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
                            virementViewModel.handleSuccessfulVirement()
                        })
                }
            })
        }


        if(fromPayment) {
            bundle = "fromPayment"

            if(paymentFourViewModel.selectedCard.value != null){
                Log.d("Card",paymentFourViewModel.selectedCard.value?.numeroCompte.toString())
                otpViewModel.otpBiometricVerifiedPayment.observe(viewLifecycleOwner) { verified ->
                    if (verified) {
                        paySelectedBillsUsingCard()
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

    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun paySelectedBills() {
        paymentViewModelUpdated.bills.value?.forEach { bill ->
            val billAmount = bill.amount
            Log.d("Bill ID",bill.id)
            Log.d("Bill Amount",billAmount.toString())
            initiatePayment(bill.id, billAmount+0.25*billAmount)

            if (selectedAccountId != null) {
                Log.d("paySelectedBills", "Selected account: $selectedAccountId")
                paymentViewModel.makePaiement(bill.amount, "paiement", selectedAccountId!!)
            } else {
                Log.e("paySelectedBills", "No account selected")
            }
        }
    }

    private fun initiatePayment(billId: String, amount: Double) {
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
            initiatePayment(bill.id, billAmount+0.25*billAmount)
            val selectedAccountNum = paymentFourViewModel.selectedCard.value?.numeroCompte
            if (selectedAccountNum != null) {
                Log.d("paySelectedBills", "Selected account (card): $selectedAccountNum")
                paymentViewModel.makePaiementUsingCard(bill.amount, "Paiement", selectedAccountNum,paymentFourViewModel.selectedCard.value!!)
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
    val binding = FragmentOtpBinding.inflate(inflater, container, false)
    binding.otp = otpViewModel

    binding.lifecycleOwner = viewLifecycleOwner

    binding.verifyButton.setOnClickListener {
        val enteredText = binding.editTextOTP.text.toString()
        otpViewModel.onButtonClickProvisoire(requireActivity(), enteredText,actualText,bundle)
        Log.e("Debug", actualText) // Log the entered text
    }

    actualText=otpViewModel.generateOTP()


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
            Log.e("OTP",actualText)
            // Permission already granted, proceed with sending SMS
            sendSMS("+2125223697854", "Code de vérification : Veuillez saisir ce code pour pouvoir continuer $actualText")
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

                sendSMS("+2125223697854", "Code de vérification : Veuillez saisir ce code pour pouvoir continuer $actualText")
            } else {
                //
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
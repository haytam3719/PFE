package com.example.project

import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.FragmentOtpBinding
import com.example.project.models.DeviceInfo
import com.example.project.viewmodels.CollectInfoViewModel
import com.example.project.viewmodels.OtpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTPHandler : Fragment() {
    companion object {
        private const val SEND_SMS_PERMISSION_REQUEST_CODE = 123
    }
    private val otpViewModel: OtpViewModel by viewModels()
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var actualText:String
    //val initialClientValue = clientViewModel.client.value



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
            otpViewModel.onButtonClickProvisoire(requireActivity(), enteredText,actualText)
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
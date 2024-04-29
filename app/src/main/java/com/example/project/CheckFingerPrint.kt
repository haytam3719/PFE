package com.example.project

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.project.databinding.FragmentFingerPrintBinding
import com.example.project.viewmodels.BiometricViewModel
import com.example.project.viewmodels.CollectInfoViewModel
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class CheckFingerPrint : Fragment() {
    private val fingerPrintViewModel: BiometricViewModel by viewModels()
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private val mainThreadExecutor: MainThreadExecutor = MainThreadExecutor()
    private val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    private lateinit var lottieAnimationView: LottieAnimationView
    // Generate a key alias
    val keyAlias = "my_key_alias"

    // Generate a symmetric key using KeyGenerator
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val keySpec = KeyGenParameterSpec.Builder(
        keyAlias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setRandomizedEncryptionRequired(false) // Required for biometric authentication
        .build()

    // Create a BiometricPrompt.CryptoObject with the Cipher
    val cryptoObject = BiometricPrompt.CryptoObject(cipher)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fingerPrintViewModel.navigateToScanCin.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navigate when event is triggered
                findNavController().navigate(R.id.print_to_sacncin)

                // Reset the navigation event after navigation
                fingerPrintViewModel.onNavigationComplete()
            }
        })

        fingerPrintViewModel.showBiometricPrompt.observe(viewLifecycleOwner) { showBiometric ->
            if (showBiometric) {
                showBiometricPrompt()
                fingerPrintViewModel.onBiometricPromptShown()
            }
        }

        keyGenerator.init(keySpec)
        keyGenerator.generateKey()

        // Retrieve the secret key from the AndroidKeyStore
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKeyEntry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        collectInfoViewModel.observeClient().observe(viewLifecycleOwner,  Observer { client ->
            // Handle client update here
            // You can update UI or perform any other actions based on the client data
            // For example, log the client data

            client.fingerPrint= fingerPrintViewModel.setGetRegisteredFingerprintData(
                fingerPrintViewModel.getBiometricPrint()!!
            )
                .toString()


            Log.e("LiveDat", client.toString())
        })

        lottieAnimationView.setAnimation("fingerprintanim.json")
        lottieAnimationView.playAnimation()

    }

    private fun showBiometricPrompt() {
        val biometricPrompt = BiometricPrompt(
            this,
            mainThreadExecutor,
            fingerPrintViewModel.getBiometricCallback()
        )
        biometricPrompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate with your fingerprint")
                .setSubtitle("Place your finger on the fingerprint sensor")
                .setNegativeButtonText("Cancel")
                .setConfirmationRequired(false)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build(),
            cryptoObject // Pass the CryptoObject to authenticate
        )

    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFingerPrintBinding.inflate(inflater, container, false)
        binding.biometricViewModel = fingerPrintViewModel
        lottieAnimationView = binding.lottieAnimationView
        binding.lifecycleOwner = viewLifecycleOwner


        fingerPrintViewModel.biometricEvent.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is BiometricViewModel.BiometricEvent.Success -> {
                    // Handle successful authentication
                    Log.d("Biometric", "Authentication succeeded")

                }
                is BiometricViewModel.BiometricEvent.Error -> {
                    // Handle authentication error
                    Log.e("Biometric", "Authentication error: ${event.errorMessage}")
                }
                is BiometricViewModel.BiometricEvent.Failure -> {
                    // Handle authentication failure
                    Log.e("Biometric", "Authentication failed: ${event.errorMessage}")
                }
            }
        })
        return binding.root
    }

}



class MainThreadExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}

























    /*
    private val clientViewModel: ClientViewModel by viewModels()
    private var clientPop:Client?=null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {}
        }}}
                //FingerPrint(unit = showBiometricPrompt(),clientViewModel)
                /*
                clientViewModel.client.observe(viewLifecycleOwner) { client ->
                    clientPop=client
                }
                clientPop?.fingerPrint =fingerPrint
            }
        }}


    @RequiresApi(Build.VERSION_CODES.P)
    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate with your fingerprint")
            .setSubtitle("Place your finger on the fingerprint sensor")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(
            this,
            MainThreadExecutor(),
            biometricAuthenticationCallback
        )
        biometricPrompt.authenticate(promptInfo)
    }

    private var temp:ByteArray? = null

    private val biometricAuthenticationCallback = @RequiresApi(Build.VERSION_CODES.P)
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.e("Error","biometricAuthenticationCallback object")
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            val fingerprintData = result.cryptoObject?.cipher?.doFinal("FingerprintData".toByteArray())
            temp=fingerprintData
            //A stocker

            Log.e("Sucess","Success")

        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Log.e("Error","onAuthenticationFailed method")
        }


    }

    private var fingerPrint=temp


    fun getClientPop(): Client? {
        return clientPop
    }

    fun getBiometricCallBack(): BiometricPrompt.AuthenticationCallback {
        return biometricAuthenticationCallback
    }


    class MainThreadExecutor : Executor {
        override fun execute(command: Runnable) {
            command.run()
        }
    }
}


@Composable
fun FingerPrint(unit:Unit,clientViewModel: ClientViewModel){

    CenterAlignedTopAppBarExample(text ="Vérification de l'empreinte digitale")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text("Veuillez poser votre doigt sur le sensor", Modifier.padding(150.dp))
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ElevatedButtonExample(text = "Vérification de l'empreinte digitale") {
                unit
                CheckFingerPrint().getClientPop()?.let { clientViewModel.updateClient(it) }
            }
        }
    }
}





fun showBiometricPrompt(
    context: FragmentActivity, // Pass the context instead of the fragment
    executor: Executor, // Pass the executor
    biometricAuthenticationCallback: BiometricPrompt.AuthenticationCallback // Pass the callback
) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authenticate with your fingerprint")
        .setSubtitle("Place your finger on the fingerprint sensor")
        .setNegativeButtonText("Cancel")
        .build()

    val biometricPrompt = BiometricPrompt(
        context,
        executor,
        biometricAuthenticationCallback
    )
    biometricPrompt.authenticate(promptInfo)
}
*/
*/
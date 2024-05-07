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
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.project.databinding.VirementBinding
import com.example.project.viewmodels.BiometricViewModel
import com.example.project.viewmodels.TransportVirementViewModel
import com.example.project.viewmodels.VirementViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@AndroidEntryPoint
class Virement : Fragment(){
    //private var virement = Virement("",0.0, "", "", CompteImpl("","", TypeCompte.COURANT,0.0,mutableListOf(),null), CompteImpl("","", TypeCompte.COURANT,0.0,mutableListOf(),null), "","",0.0,"")
    private val virementViewModel:VirementViewModel by viewModels()
    private val fingerPrintViewModel:BiometricViewModel by viewModels()
    private val mainThreadExecutor: MainThreadExecutor = MainThreadExecutor()
    private val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    val bundle = Bundle().apply {
        putBoolean("fromVirement", true)
    }
    private val transportVirementViewModel: TransportVirementViewModel by activityViewModels()


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

        virementViewModel.virementLiveData.observe(viewLifecycleOwner) { virement ->

            //Log.d("Virement Details", "Émetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}, Montant: ${virement.montant}")
        }

        /*

        virementViewModel.navigateToOtp.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(com.example.project.R.id.virement_to_otp,bundle)

                    // Reset the navigation event after navigation
                    virementViewModel.onNavigationComplete()
                }
            })

         */



        fingerPrintViewModel.navigateToOtp.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    virementViewModel.virementLiveData.observe(viewLifecycleOwner) { virement ->
                        Log.e("Virement Details from Virement Fragment", "Émetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}, Montant: ${virement.montant}")
                            //Log.d("Virement Details", "Émetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}, Montant: ${virement.montant}")

                        transportVirementViewModel.setVirement(virement)

                    }
                    // Navigate when event is triggered
                    findNavController().navigate(R.id.virement_to_otp, bundle)


                    // Reset the navigation event after navigation
                    fingerPrintViewModel.onNavigationCompleteOtp()
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
        val binding = VirementBinding.inflate(inflater, container, false)
        binding.virementViewModel = virementViewModel
        binding.fingerPrintViewModel = fingerPrintViewModel
        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root

    }




}



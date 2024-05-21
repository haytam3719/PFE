package com.example.project

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.VirementStepFourBinding
import com.example.project.models.CircularProgressView
import com.example.project.viewmodels.BiometricViewModel
import com.example.project.viewmodels.ProgressBarViewModel
import com.example.project.viewmodels.TransportVirementViewModel
import com.example.project.viewmodels.VirementUpdatedViewModel
import com.example.project.viewmodels.VirementViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@AndroidEntryPoint
class VirementStepFour : Fragment() {
    private val progressViewModel: ProgressBarViewModel by activityViewModels()
    private val sharedViewModel:VirementUpdatedViewModel by activityViewModels()
    private var _binding: VirementStepFourBinding? = null
    private val binding get() = _binding!!


    //FingerPrint details
    private val virementViewModel: VirementViewModel by viewModels()
    private val fingerPrintViewModel: BiometricViewModel by viewModels()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VirementStepFourBinding.inflate(inflater, container, false)
        binding.fingerPrintViewModel = fingerPrintViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val circularProgressBar = binding.circularProgressBar
        circularProgressBar.progress = 75
        Handler(Looper.getMainLooper()).postDelayed({
            animateProgress(circularProgressBar, 100) {
                progressViewModel.setProgress(100)
            }
        }, 500)



        sharedViewModel.selectedAccount.observe(viewLifecycleOwner) { account ->
            binding.compteEmet.text = "Compte Emetteur\n${account.accountNumber}"
            virementViewModel.virementLiveData.value!!.compteEmet.numero = account.accountNumber
        }

        sharedViewModel.selectedClient.observe(viewLifecycleOwner) { client ->
            binding.compteBenef.text = "Compte Bénéficiaire\n${client.accountNumber}"
            virementViewModel.virementLiveData.value!!.compteBenef.numero = client.accountNumber

        }

        sharedViewModel.amount.observe(viewLifecycleOwner) { amount ->
            binding.montant.text = "Montant de l'opération: $amount DH"
            virementViewModel.virementLiveData.value!!.montant = amount?.toDouble()!!


        }

        sharedViewModel.motif.observe(viewLifecycleOwner) { motif ->
            binding.motif.text = "Motif: $motif"
            virementViewModel.virementLiveData.value!!.motif = motif

        }

        sharedViewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val dateText = date ?: "Immédiate"
            binding.dateOperation.text = "Date d'exécution de l'opération\n$dateText"
        }


        fingerPrintViewModel.navigateToOtp.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    virementViewModel.virementLiveData.observe(viewLifecycleOwner) { virement ->
                        Log.e("Virement Details from Virement Fragment", "Émetteur: ${virement.compteEmet}, Bénéficiaire: ${virement.compteBenef}, Montant: ${virement.montant}")

                        transportVirementViewModel.setVirement(virement)

                    }
                    findNavController().navigate(R.id.virementStepFour_to_otp, bundle)


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

    private fun animateProgress(progressView: CircularProgressView, toProgress: Int, onEnd: () -> Unit) {
        val animator = ValueAnimator.ofInt(progressView.progress, toProgress)
        animator.duration = 500
        animator.addUpdateListener { animation ->
            progressView.updateProgress(animation.animatedValue as Int)
        }
        animator.addListener(onEnd = { onEnd() })
        animator.start()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

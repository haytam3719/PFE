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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.AccountData
import com.example.project.databinding.RechargeSimple3Binding
import com.example.project.models.Bill
import com.example.project.viewmodels.BiometricViewModel
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.PaymentViewModelUpdated
import com.example.project.viewmodels.RechargeViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@AndroidEntryPoint
class Recharge3 : Fragment() {

    private var _binding: RechargeSimple3Binding? = null
    private val binding get() = _binding!!

    private val consultationViewModel: ConsultationViewModel by activityViewModels()
    private val rechargeViewModel: RechargeViewModel by activityViewModels()
    private val paymentViewModelUpdated: PaymentViewModelUpdated by activityViewModels()

    private lateinit var adapter: AccountsAdapter
    private var phoneNumberr = ""
    private var montantt = ""

    private val fingerPrintViewModel: BiometricViewModel by viewModels()
    private val mainThreadExecutor: MainThreadExecutor = MainThreadExecutor()
    private val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    private var selectedAccountId: String = ""


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
    ): View? {
        _binding = RechargeSimple3Binding.inflate(inflater, container, false)
        binding.fingerPrintViewModel = fingerPrintViewModel
        Log.d("Recharge3", "onCreateView: View created")

        setupClickListeners()
        adapter = AccountsAdapter { account ->
            consultationViewModel.selectAccount(account)
        }

        binding.listViewOptions.adapter = adapter
        binding.listViewOptions.layoutManager = LinearLayoutManager(context)

        observeAccounts()
        consultationViewModel.fetchAccountsForCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)

        observeViewModel()


        fingerPrintViewModel.navigateToOtp.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    val bundle = Bundle().apply {
                        putBoolean("fromPayment", true)
                        putString("selectedAccountId", selectedAccountId)
                    }
                    findNavController().navigate(R.id.recharge3_to_otp, bundle)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Recharge3", "onViewCreated: View created")

        rechargeViewModel.phoneNumber.observe(viewLifecycleOwner) { phoneNumber ->
            Log.d("Recharge3", "Phone number observed: $phoneNumber")
            binding.textInputLayoutPhoneNumber.editText?.setText(phoneNumber)
            phoneNumberr = "+212$phoneNumber"
            updateBillIfReady()
        }
        rechargeViewModel.montant.observe(viewLifecycleOwner) { montant ->
            Log.d("Recharge3", "Montant observed: $montant")
            binding.tvAmount.text = "Montant sélectionné: $montant"
            montantt = montant
            updateBillIfReady()
        }
        rechargeViewModel.rechargeType.observe(viewLifecycleOwner) { rechargeType ->
            Log.d("Recharge3", "Type de Recharge observed: $rechargeType")
            binding.tvType.text = "Type de recharge sélectionné: $rechargeType"
        }

        val bill = Bill(phoneNumberr,montantt.extractAmount(),"","")
        Log.d("CHECK BILLS FORM","$phoneNumberr $montantt")
        val listBill = listOf<Bill>(bill)
        Log.d("Bill From RECHARGE 3",listBill.toString())
        paymentViewModelUpdated.loadBills(listBill)
    }




    private fun updateBillIfReady() {
        val phoneNumber = phoneNumberr
        val montant = montantt

        if (!phoneNumber.isNullOrEmpty() && !montant.isNullOrEmpty()) {
            val bill = Bill(phoneNumber, montant.extractAmount(), "", "")
            Log.d("CHECK BILLS FORM", "$phoneNumber $montant")
            val listBill = listOf(bill)
            Log.d("Bill From RECHARGE 3", listBill.toString())
            paymentViewModelUpdated.loadBills(listBill)
        }
    }




    private fun updateSelectedAccountUI(account: AccountData) {
        binding.clPaymentAccount.apply {
            binding.tvAccountName.text = formatAccountType(account.accountType)
            binding.tvAccountNumber.text = "Numéro de compte: ${account.accountNumber}"
            binding.tvBalance.text = "Solde: ${account.balance} DH"
            selectedAccountId = account.accountNumber
            Log.d("Selected Account",selectedAccountId)
            binding.ivDropdown.visibility = View.GONE
        }
        binding.popupView.visibility = View.GONE
        binding.buttonPay.visibility = View.VISIBLE
        binding.buttonCancel.visibility = View.VISIBLE
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

    private fun formatAccountType(type: String): String {
        return when (type) {
            "CHEQUES" -> "Compte chèque"
            "COURANT" -> "Compte courant"
            "EPARGNE" -> "Compte épargne"
            else -> type
        }
    }

    private fun observeViewModel() {
        consultationViewModel.selectedAccount.observe(viewLifecycleOwner) { selectedAccount ->
            updateSelectedAccountUI(selectedAccount)
        }
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


    fun String.extractAmount(): Double {
        // Use a regular expression to find the numeric part
        val regex = Regex("""(\d+)\s*DH""")
        val matchResult = regex.find(this)
        val amountString = matchResult?.groups?.get(1)?.value ?: "0"
        return amountString.toDouble()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Recharge3", "onDestroyView: View destroyed")
        _binding = null
    }
}

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
import com.example.project.adapters.BillAdapter2
import com.example.project.adapters.CardsAdapter
import com.example.project.databinding.Payment4Binding
import com.example.project.models.Bill
import com.example.project.viewmodels.BiometricViewModel
import com.example.project.viewmodels.CardsViewModel
import com.example.project.viewmodels.ConsultationViewModel
import com.example.project.viewmodels.PaymentFourViewModel
import com.example.project.viewmodels.PaymentViewModelUpdated
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@AndroidEntryPoint
class PaymentStepFour : Fragment() {
    private val consultationViewModel: ConsultationViewModel by viewModels()
    private val paymentViewModelUpdated:PaymentViewModelUpdated by activityViewModels()
    private val paymentFourViewModel: PaymentFourViewModel by activityViewModels()
    private lateinit var adapter: AccountsAdapter

    private var _binding: Payment4Binding? = null
    private val binding get() = _binding!!

    private lateinit var selectedBillIds: List<String>
    private lateinit var selectedBillAmounts: DoubleArray
    private lateinit var selectedBillDueDates: List<String>
    private lateinit var totalApayer:String
    private lateinit var billAdapter: BillAdapter2
    private val cardsViewModel: CardsViewModel by viewModels()
    private lateinit var cardsAdapter: CardsAdapter


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
        _binding = Payment4Binding.inflate(inflater, container, false)
        binding.fingerPrintViewModel = fingerPrintViewModel
        setupClickListeners()
        adapter = AccountsAdapter { account ->
            consultationViewModel.selectAccount(account)
        }

        binding.listViewOptions.adapter = adapter
        binding.listViewOptions.layoutManager = LinearLayoutManager(context)

        observeAccounts()
        consultationViewModel.fetchAccountsForCurrentUser(FirebaseAuth.getInstance().currentUser!!.uid)

        observeViewModel()

        arguments?.let {
            selectedBillIds = it.getStringArray("numbers") ?.toList()!!
            selectedBillAmounts = it.getDoubleArray("amounts") ?: doubleArrayOf()
            selectedBillDueDates = it.getStringArray("dueDates")?.toList()!!
            totalApayer = it.getString("totalApayer").toString()

            binding.tvTotalAmount.text = totalApayer.toString()

            Log.d("selectedBillIds", selectedBillIds.toString())
            Log.d("selectedBillAmounts", selectedBillAmounts.contentToString())
            Log.d("selectedBillDueDates", selectedBillDueDates.toString())

        }


        fingerPrintViewModel.navigateToOtp.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    val bundle = Bundle().apply {
                        putBoolean("fromPayment", true)
                        putString("selectedAccountId", selectedAccountId)
                    }

                    findNavController().navigate(R.id.paymentStepFour_to_otp, bundle)

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
        setupRecyclerView()

        //Cards Code
        cardsAdapter = CardsAdapter(emptyList()) { selectedCard ->
            paymentFourViewModel.selectCard(selectedCard)
        }

        binding.listViewOptionsCarte.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cardsAdapter
        }

        // Observe the LiveData from the ViewModel
        cardsViewModel.cartesByProprietaire.observe(viewLifecycleOwner, Observer { cards ->
            cardsAdapter.updateCards(cards)
        })

        cardsViewModel.cartesByProprietaire.observe(viewLifecycleOwner, Observer { cards ->
            cardsAdapter.updateCards(cards)
        })


        // Observe the selected card LiveData
        paymentFourViewModel.selectedCard.observe(viewLifecycleOwner, Observer { selectedCard ->
            binding.popupViewCarte.visibility = View.GONE
            binding.tvCardDetails.text = "Numéro de la carte:\n **** **** **** ${selectedCard.numeroCarte.takeLast(4)}\nType carte: ${if (selectedCard.numeroCompte != null) "Débit" else "Crédit"}"
            binding.ivCardDropdown.visibility = View.GONE
        })

        val proprietorId = FirebaseAuth.getInstance().currentUser!!.uid
        cardsViewModel.getCartesByIdProprietaire(proprietorId)


        binding.clCardSelection.setOnClickListener {
            binding.popupViewCarte.visibility = View.VISIBLE
        }

        binding.closeImageViewCarte.setOnClickListener{
            binding.popupViewCarte.visibility = View.GONE
        }

    }


    private fun setupRecyclerView() {
        val bills = selectedBillIds.mapIndexed { index, id ->
            Bill(id, selectedBillAmounts[index],selectedBillDueDates[index], selectedBillIds[index])
        }

        paymentViewModelUpdated.loadBills(bills)

        billAdapter = BillAdapter2(bills)
        binding.recyclerViewBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = billAdapter
        }
    }


    private fun updateSelectedAccountUI(account: AccountData) {
        binding.clPaymentAccount.apply {
            binding.tvAccountName.text = formatAccountType(account.accountType)
            binding.tvAccountNumber.text = "Numéro de compte: ${account.accountNumber}"
            binding.tvBalance.text = "Solde: ${account.balance} DH"
            selectedAccountId = account.accountNumber
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


    private fun observeViewModel() {
        consultationViewModel.selectedAccount.observe(viewLifecycleOwner) { selectedAccount ->
            updateSelectedAccountUI(selectedAccount)
        }

    }

}
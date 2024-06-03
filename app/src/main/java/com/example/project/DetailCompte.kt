package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapters.TransactionAdapter
import com.example.project.databinding.DetailCompteBinding
import com.example.project.models.CompteImpl
import com.example.project.models.TransactionImpl
import com.example.project.prototype.Transaction
import com.example.project.prototype.TypeCompte
import com.example.project.viewmodels.DetailCompteViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.text.Normalizer
import java.util.Locale

@AndroidEntryPoint
class DetailCompte : Fragment() {
    private var _binding: DetailCompteBinding? = null
    private val binding get() = _binding!!

    private val detailCompteViewModel: DetailCompteViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    private var accountNumberG: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailCompteBinding.inflate(inflater, container, false)

        detailCompteViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })


        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        adapter = TransactionAdapter(emptyList(),FirebaseAuth.getInstance().currentUser?.uid ?: "",object : TransactionAdapter.OnRecycleViewItemClickListener {

            override fun onItemClick(transactionData: Transaction) {
                val bundle = bundleOf(
                    "transactionId" to transactionData.idTran
                )
                findNavController().navigate(R.id.detailCompte_to_detailTransaction, bundle)
            }
        })

        arguments?.let { bundle ->
            val accountType = bundle.getString("accountType")
            val accountNumber = bundle.getString("accountNumber")
            val balance = bundle.getString("balance")
            accountNumberG = accountNumber

            binding.accountType.text = accountType
            binding.accountNumber.text = accountNumber
            binding.accountBalance.text = "$balance DH"
            binding.balance.text = "$balance DH"
        }

        setupRecyclerView()
        accountNumberG?.let {
            detailCompteViewModel.loadTransactionsForSingleAcc(it)
        }
        observeViewModel()

        binding.detailCompte = this

        detailCompteViewModel.deletionSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Votre compte a été supprimé avec succès", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        detailCompteViewModel.deletionError.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }


        detailCompteViewModel.updateStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Account updated successfully.", Toast.LENGTH_SHORT).show()
            }
        }

        detailCompteViewModel.updateError.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupRecyclerView() {
        binding.recentTransactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DetailCompte.adapter
        }
    }

    private fun observeViewModel() {
        detailCompteViewModel.transactionsForSingleAcc.observe(viewLifecycleOwner, Observer { transactions ->
            if(transactions.isNullOrEmpty()){
                binding.tvOperations.visibility = View.VISIBLE
            }
            adapter.updateTransactions(transactions)
        })

        detailCompteViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        })
    }

    fun onClickEditAccount(view:View){
        showChangeAccountTypeDialog()
    }

    fun onClickDeleteAccount(view:View){
        showDeleteAccountConfirmationDialog()
    }

    fun showChangeAccountTypeDialog() {
        val accountTypes = arrayOf("Courant", "Épargne", "Chèques")
        var currentSelection = -1
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Account Type")

        builder.setSingleChoiceItems(accountTypes, -1) { dialog, which ->
            currentSelection = which
        }

        builder.setPositiveButton("Valider") { dialog, which ->
            if (currentSelection != -1) {
                val selectedType = accountTypes[currentSelection]
                updateAccountType(selectedType)
            }
        }

        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }


    fun normalizeType(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").toUpperCase(
            Locale.ROOT)
    }

    private fun updateAccountType(selectedType: String) {
        val normalizedType = normalizeType(selectedType)
        val typeEnum: TypeCompte = try {
            enumValueOf<TypeCompte>(normalizedType)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(context, "Invalid account type selected.", Toast.LENGTH_SHORT).show()
            return
        }


        val currentAccountNumber = accountNumberG
        val currentOwnerID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val currentBalance = binding.accountBalance.text.toString().removePrefix("Solde: ").removeSuffix(" DH").toDouble()
        val currentTransactionHistory = adapter.getCurrentTransactions()

        val updatedAccount = CompteImpl(
            currentAccountNumber!!,
            currentOwnerID,
            typeEnum,
            currentBalance,
            currentTransactionHistory as MutableList<TransactionImpl>
        )

        currentAccountNumber.let {
            detailCompteViewModel.updateAccount(it, updatedAccount)
            Toast.makeText(context, "Account type updated to $selectedType.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirmer la suppression")
            setMessage("Vous êtes sûr de vouloir supprimer votre compte ? Cette operation est irréversible.")

            setPositiveButton("Supprimer") { dialog, which ->
                detailCompteViewModel.deleteAccount(accountNumberG!!)
            }

            setNegativeButton("Cancel", null)

            show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

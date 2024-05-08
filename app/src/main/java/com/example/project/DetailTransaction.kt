package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.DetailOperationBinding
import com.example.project.prototype.Transaction
import com.example.project.viewmodels.DetailTransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailTransaction : Fragment() {

    private var _binding: DetailOperationBinding? = null
    private val binding get() = _binding!!
    private val detailTransactionsViewModel: DetailTransactionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DetailOperationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("transactionId")?.let { transactionId ->
            Log.d("TransactionId",transactionId)
            detailTransactionsViewModel.loadTransaction(transactionId)
            observeTransactionDetails()
        }
    }

    private fun observeTransactionDetails() {
        detailTransactionsViewModel.transactionDetails.observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                Log.d("Transaction", transaction.toString())
                updateTransactionDetails(it)
            }
        }
    }

    private fun updateTransactionDetails(transaction: Transaction) {
        binding.tvTransactionId.text = "ID: ${transaction.idTran}"
        binding.tvTransactionType.text = "Type de la transaction: ${transaction.typeTransaction}"
        binding.tvMotive.text = "Motif: ${transaction.motif}"
        binding.tvPaymentMethod.text = "Méthode: ${transaction.methodPaiement}"
        binding.tvAmount.text = "Montant: ${transaction.montant.toString()}"
        binding.tvStatus.text = "Statut: ${transaction.statut}"
        binding.tvFees.text = "Frais: ${transaction.fraisTrans.toString()}"
        binding.tvRecipientId.text = "ID du bénéficiaire: ${transaction.compteBenef.id_proprietaire}"
        binding.tvRecipientAccountNumber.text = "Numéro de compte du bénéficiare: ${transaction.compteBenef.numero}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

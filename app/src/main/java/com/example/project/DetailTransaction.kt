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
import com.google.firebase.auth.FirebaseAuth
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
        binding.operationDate.text = transaction.date
        binding.tvTransactionType.text =transaction.typeTransaction
        binding.tvMotive.text = transaction.motif
        binding.tvPaymentMethod.text = transaction.methodPaiement
        binding.tvStatus.text = transaction.statut
        binding.tvFees.text = "${transaction.fraisTrans.toString()} DH"
        binding.amount.text = "${transaction.montant.toString()} DH"
        binding.operationDescription.text = transaction.motif
        if(transaction.compteBenef.id_proprietaire == FirebaseAuth.getInstance().currentUser!!.uid){
            binding.tvRecipientAccountNumberLabel.text = "Numéro de compte de l'émetteur"
            binding.tvRecipientAccountNumber.text = transaction.compteEmet.numero
            binding.operationAmount.text = "${transaction.montant.toString()} DH"
        }else{
            binding.tvRecipientAccountNumberLabel.text = "Numéro de compte du bénéficiaire"
            binding.tvRecipientAccountNumber.text = transaction.compteBenef.numero
            binding.operationAmount.text = "-${transaction.montant.toString()} DH"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

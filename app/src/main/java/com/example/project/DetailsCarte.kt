package com.example.project

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.adapters.TransactionAdapter
import com.example.project.databinding.DetailsCarteBinding
import com.example.project.models.CarteImpl
import com.example.project.models.TransactionImpl
import com.example.project.viewmodels.CardsViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsCarte : Fragment(),ModifySecurityCodeDialogFragment.OnSecurityCodeModifiedListener {

    private val cardsViewModel:CardsViewModel by viewModels()
    private var currentCard = CarteImpl()
    private var id_carte:String? = null
    private var accountNumbers = emptyList<String>()
    private lateinit var transactionAdapter: TransactionAdapter

    private var _binding: DetailsCarteBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardsViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            if(currentCard.numeroCompte!=null){
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }else{
                binding.progressBar.visibility = View.GONE
            }
        })


        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        val id_proprietaire = FirebaseAuth.getInstance().currentUser!!.uid

        transactionAdapter = TransactionAdapter(emptyList(), id_proprietaire, object : TransactionAdapter.OnRecycleViewItemClickListener {

            override fun onItemClick(transactionData: com.example.project.prototype.Transaction) {
                val bundle = bundleOf(
                    "transactionId" to transactionData.idTran
                )
                findNavController().navigate(R.id.detailsCarte_to_detailTransaction, bundle)
            }
        })


        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = transactionAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val accountNumber = currentCard.numeroCompte

        accountNumber?.let {
            Log.d("Cards", it)
            cardsViewModel.fetchTransactionsByPaymentMethod(it)
        }

        val allTransactions = mutableListOf<TransactionImpl>()

        cardsViewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if(transactions.isNullOrEmpty()){
                if(currentCard.numeroCompte == null){
                    binding.tvTransactionsCarte.setText("Les transactions en utilisant des cartes Crédit ne sont pas encore prises en charge, puisqu'elles ne sont pas associée à un compte spécifique.")
                }else{
                    binding.tvTransactionsCarte.visibility = View.VISIBLE
                }

            }
            transactions?.let {
                val filteredTransactions = it.filter { transaction ->
                    val regex = Regex("""\d{4} \d{4} \d{4} \d{4}""")
                    val matchResult = regex.find(transaction.methodPaiement)
                    val cardNumber = matchResult?.value ?: ""
                    Log.d("Filtering", "Extracted Card Number: $cardNumber, Current Card: ${currentCard.numeroCarte}")
                    cardNumber == currentCard.numeroCarte
                }
                Log.d("Filtered Transactions", "Count: ${filteredTransactions.size}")
                transactionAdapter.updateTransactions(filteredTransactions)
                allTransactions.addAll(filteredTransactions)
                filteredTransactions.forEach { transaction ->
                    Log.d("Transaction Details", "Transaction ID: ${transaction.idTran}, Amount: ${transaction.montant}, Date: ${transaction.date}, Method: ${transaction.methodPaiement}")
                }
            }
        }

        cardsViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }





    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailsCarteBinding.inflate(inflater, container, false)

        binding.detailsCarte = this
        arguments?.let { bundle ->
            val idCarte = bundle.getString("idCarte")
            val idProprietaireCarte = bundle.getString("id_proprietaire_carte")
            val numeroCarte = bundle.getString("numeroCarte")
            val dateExpiration = bundle.getString("dateExpiration")
            val codeSecurite = bundle.getString("codeSecurite")
            val nomTitulaire = bundle.getString("nomTitulaire")
            val adresseFacturation = bundle.getString("adresseFacturation")
            val limiteCredit = bundle.getDouble("limiteCredit", 0.0)
            val numeroCompte = bundle.getString("numeroCompte")

            val tvCardNumber: TextView = binding.tvCardNumber
            val tvCardHolder: TextView = binding.tvCardHolder
            val tvExpiryDate: TextView = binding.tvExpiryDate
            val tvSecurityCode: TextView = binding.tvSecurityCode
            val tvBillingAddress: TextView = binding.tvBillingAddress
            val tvCreditLimit: TextView = binding.tvCreditLimit
            val tvAccountNumber: TextView = binding.tvAccountNumber

            currentCard = CarteImpl(idCarte!!,idProprietaireCarte!!,numeroCarte!!,dateExpiration!!,codeSecurite!!,nomTitulaire!!,adresseFacturation!!,limiteCredit, numeroCompte)

            if (currentCard.loadBlockStatus(requireContext())) {
                binding.cardInfos.setBackgroundResource(R.drawable.card_background_gradient_bloqued)
                binding.btnBlockCard.text = "Débloquer carte"
                binding.btnBlockCard.setBackgroundColor(android.graphics.Color.parseColor("#757575"))
            } else {
                binding.cardInfos.setBackgroundResource(R.drawable.card_background_gradient)
                binding.btnBlockCard.text = "Bloquer carte"
                binding.btnBlockCard.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            }

            tvCardNumber.text = "**** **** **** ${numeroCarte?.takeLast(4)}"
            tvCardHolder.text = "Titulaire: $nomTitulaire"
            tvExpiryDate.text = "Date d'expiration: $dateExpiration"
            tvSecurityCode.text = "CVC: $codeSecurite"
            tvBillingAddress.text = "Adresse de facturation: $adresseFacturation"

            if(numeroCompte == null){
                tvAccountNumber.visibility = View.GONE    //Carte Crédit
                tvCreditLimit.visibility = View.VISIBLE
                tvCreditLimit.text = "Limite: ${limiteCredit} DH"

                val layoutParams = tvCreditLimit.layoutParams as? ViewGroup.MarginLayoutParams
                    ?: ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val marginInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
                layoutParams.bottomMargin = marginInPixels

                tvCreditLimit.layoutParams = layoutParams


            }else {
                tvCreditLimit.visibility = View.GONE      //Carte Débit
                tvAccountNumber.visibility = View.VISIBLE
                tvAccountNumber.text = "Numéro de compte:\n$numeroCompte"
            }

            id_carte = idCarte

        }


        return binding.root

    }

    fun onClickModifierCodeSec(view: View){
        showModifySecurityCodeDialog()
    }
    fun onClickSupprimerCarte(view:View){
        showConfirmDeleteDialog()
    }

    fun onClickBloquerCarte(view: View) {
        val cardStatus = currentCard.loadBlockStatus(requireContext())
        if (cardStatus) {
            currentCard.debloquerCarte(requireContext())
            Toast.makeText(requireContext(), "La carte a été débloquée", Toast.LENGTH_SHORT).show()
            binding.cardInfos.setBackgroundResource(R.drawable.card_background_gradient)  // Set to normal background
            binding.btnBlockCard.text = "Bloquer carte"
            binding.btnBlockCard.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        } else {
            currentCard.bloquerCarte(requireContext())
            Toast.makeText(requireContext(), "La carte a été bloquée avec succès", Toast.LENGTH_SHORT).show()
            binding.cardInfos.setBackgroundResource(R.drawable.card_background_gradient_bloqued)  // Set to blocked background
            binding.btnBlockCard.text = "Débloquer carte"
            binding.btnBlockCard.setBackgroundColor(android.graphics.Color.parseColor("#757575"))
        }
    }


    private fun showModifySecurityCodeDialog() {
        val dialog = ModifySecurityCodeDialogFragment()
        dialog.listener = this
        dialog.show(parentFragmentManager, "ModifySecurityCodeDialogFragment")
    }

    private fun showConfirmDeleteDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmer la supression")
        builder.setMessage("Êtes-vous sûr de vouloir supprimer cette carte?")
        builder.setPositiveButton("Supprimer") { dialog, _ ->
            cardsViewModel.supprimerCarte(id_carte!!)
            Toast.makeText(requireContext(),"La carte a été supprimée avec succès",Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("Garder") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onSecurityCodeModified(newSecurityCode: String) {
        cardsViewModel.modifierSecurityCode(id_carte!!,newSecurityCode)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
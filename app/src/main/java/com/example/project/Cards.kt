package com.example.project

import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.project.adapters.CardAdapter
import com.example.project.adapters.CardsPageTransformer
import com.example.project.adapters.TransactionAdapter
import com.example.project.databinding.MesCartesBinding
import com.example.project.models.CarteImpl
import com.example.project.models.TransactionImpl
import com.example.project.viewmodels.CardsViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Cards : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: CardAdapter
    private val cardsViewModel:CardsViewModel by viewModels()
    private var accountNumbers = emptyList<String>()
    private lateinit var transactionAdapter: TransactionAdapter
    private var _binding: MesCartesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardsViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
                findNavController().navigate(R.id.mesCartes_to_detailTransaction, bundle)
            }
        })


        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = transactionAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        cardsViewModel.getCartesByIdProprietaire(id_proprietaire)
        cardsViewModel.cartesByProprietaire.observe(viewLifecycleOwner) { cartes ->
            if(cartes.isNullOrEmpty()){
                binding.tvCards.visibility = View.VISIBLE
                binding.textView4.visibility = View.GONE
            }
            adapter.updateCards(cartes)
            val accountsNumbers = cartes.map { it.numeroCompte }
            Log.d("Cards Accounts",accountsNumbers.toString())
             accountNumbers = accountsNumbers as List<String>
            Log.d("Ensure casting",accountNumbers.toString())


            accountNumbers.forEach { accountNumber ->
                Log.d("Cards", accountNumber ?: "Null account number")
                accountNumber?.let {
                    cardsViewModel.fetchTransactionsByPaymentMethod(it)
                }
            }
        }


        val allTransactions = mutableListOf<TransactionImpl>()

        cardsViewModel.transactions.observe(viewLifecycleOwner, Observer { transactions ->
            transactions?.let {
                transactionAdapter.updateTransactions(it)
                allTransactions.addAll(it)
                it.forEach { transaction ->
                    Log.d("Transaction Details", "Transaction ID: ${transaction.idTran}, Amount: ${transaction.montant}, Date: ${transaction.date}, Method: ${transaction.methodPaiement}")
                }
            }
        })

        cardsViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })



    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MesCartesBinding.inflate(inflater, container, false)
        binding.cards = this
        adapter = CardAdapter(emptyList(), object : CardAdapter.OnItemClickListener {
            override fun onItemClick(carte: CarteImpl) {
                val bundle = bundleOf(
                    "idCarte" to carte.idCarte,
                    "id_proprietaire_carte" to carte.id_proprietaire_carte,
                    "numeroCarte" to carte.numeroCarte,
                    "dateExpiration" to carte.dateExpiration,
                    "codeSecurite" to carte.codeSecurite,
                    "nomTitulaire" to carte.nomTitulaire,
                    "adresseFacturation" to carte.adresseFacturation,
                    "limiteCredit" to carte.limiteCredit,
                    "numeroCompte" to carte.numeroCompte
                )
                findNavController().navigate(R.id.mesCartes_to_detailsCarte, bundle)
            }
        })

        viewPager = binding.viewPager
        viewPager.adapter = adapter

        viewPager.offscreenPageLimit = 1
        viewPager.setPageTransformer(CardsPageTransformer())
        viewPager.addItemDecoration(MarginItemDecoration(20))
        val btnShowPopup = binding.btnShowPopup
        val popupView = binding.popupView

        btnShowPopup.setOnClickListener {
            TransitionManager.beginDelayedTransition(container, Slide(Gravity.BOTTOM))
            popupView.visibility = if (popupView.visibility == View.GONE) View.VISIBLE else View.GONE
        }



        return binding.root


    }


    fun onClickCommanderCarte(view:View){
        findNavController().navigate(R.id.mesCartes_to_commanderCarte)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Recharge3", "onDestroyView: View destroyed")
        _binding = null
    }
}
package com.example.project

import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.project.adapters.CardAdapter
import com.example.project.adapters.CardsPageTransformer
import com.example.project.databinding.MesCartesBinding
import com.example.project.models.CarteImpl
import com.example.project.viewmodels.CardsViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Cards : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: CardAdapter
    private val cardsViewModel:CardsViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id_proprietaire = FirebaseAuth.getInstance().currentUser!!.uid
        cardsViewModel.getCartesByIdProprietaire(id_proprietaire)
        cardsViewModel.cartesByProprietaire.observe(viewLifecycleOwner) { cartes ->
            adapter.updateCards(cartes)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MesCartesBinding.inflate(inflater, container, false)
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
}
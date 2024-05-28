package com.example.project

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.MenuConnecteBinding

class MenuConnecte : Fragment() {
    private var _binding: MenuConnecteBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuConnecteBinding.inflate(inflater, container, false)
        binding.menuConnecte = this


        return binding.root
    }

    private fun toggleContent(icon: ImageView, vararg views: View) {
        val isVisible = views[0].visibility == View.VISIBLE
        if (isVisible) {
            collapseViews(icon, *views)
        } else {
            expandViews(icon, *views)
        }
    }


    private fun collapseViews(icon: ImageView, vararg views: View) {
        icon.animate().rotation(0f).setDuration(300).start()
        for (view in views) {
            view.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
        }
    }

    private fun expandViews(icon: ImageView, vararg views: View) {
        icon.animate().rotation(90f).setDuration(300).start()
        for (view in views) {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
        }
    }



    fun onClickTransferts(view: View){
        findNavController().navigate(R.id.menuConnecte_to_transfers)
    }

    fun onClickPaiements(view: View){
        findNavController().navigate(R.id.menuConnecte_to_paymentStepOne)
    }

    fun onClickConsultation(view: View){
        //findNavController().navigate(R.id.menuConnecte_to_consultation)
        toggleContent(
            binding.imageEndConsultation,
            binding.cardMesComptes,
            binding.cardMesCartes,
            binding.cardMesBeneficiaires
        )
    }

    fun onClickConsultationCompte(view: View) {
        findNavController().navigate(R.id.menuConnecte_to_consultation)
    }

    fun onClickAttijariSecure(view:View){
        findNavController().navigate(R.id.menuConnecte_to_attijariSecure)
    }

    fun onClickAssistanceReclamation(view: View){
        findNavController().navigate(R.id.menuConnecte_to_assistanceReclamation)
    }

    fun onClickInfosUtiles(view: View){
        findNavController().navigate(R.id.menuConnecte_to_infosUtiles)
    }

    fun onClickParametres(view: View){
        findNavController().navigate(R.id.menuConnecte_to_parametres)
    }

    fun onClickButtonCards(view: View){
        findNavController().navigate(R.id.menuConnecte_to_mesCartes)
    }


    fun onClickButtonMesBenef(view:View){
        findNavController().navigate(R.id.menuConnecte_to_mesBenef)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Recharge3", "onDestroyView: View destroyed")
        _binding = null
    }
}
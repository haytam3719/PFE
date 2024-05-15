package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.DetailsCarteBinding
import com.example.project.viewmodels.CardsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsCarte : Fragment(),ModifySecurityCodeDialogFragment.OnSecurityCodeModifiedListener {

    private val cardsViewModel:CardsViewModel by viewModels()
    private var id_carte:String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DetailsCarteBinding.inflate(inflater, container, false)

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

            tvCardNumber.text = "**** **** **** ${numeroCarte?.takeLast(4)}"
            tvCardHolder.text = nomTitulaire
            tvExpiryDate.text = dateExpiration
            tvSecurityCode.text = codeSecurite
            tvBillingAddress.text = adresseFacturation

            if(numeroCompte == null){
                tvAccountNumber.visibility = View.GONE    //Carte Crédit
                tvCreditLimit.visibility = View.VISIBLE
                tvCreditLimit.text = "Limite: ${limiteCredit} DH"
            }else {
                tvCreditLimit.visibility = View.GONE      //Carte Débit
                tvAccountNumber.visibility = View.VISIBLE
                tvAccountNumber.text = "Numéro de compte: $numeroCompte"
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

    fun onClickBloquerCarte(view: View){

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
}
package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.project.databinding.DialogModifySecurityCodeBinding
import com.example.project.viewmodels.CardsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModifySecurityCodeDialogFragment : DialogFragment() {
    private val cardsViewModel:CardsViewModel by viewModels()
    interface OnSecurityCodeModifiedListener {
        fun onSecurityCodeModified(newSecurityCode: String)
    }

    var listener: OnSecurityCodeModifiedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DialogModifySecurityCodeBinding.inflate(inflater, container, false)

        val securityCodeInput = binding.securityCodeInput
        val confirmButton = binding.confirmButton

        confirmButton.setOnClickListener {
            val newSecurityCode = securityCodeInput.text.toString()
            if(newSecurityCode.length!=3){
                Toast.makeText(requireContext(),"Code Invalide. Veuillez entrer un code de sécurité à trois chiffres",Toast.LENGTH_LONG).show()
            }else {
                listener?.onSecurityCodeModified(newSecurityCode)
                Toast.makeText(
                    requireContext(),
                    "Code de sécurité modifié avec succès",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

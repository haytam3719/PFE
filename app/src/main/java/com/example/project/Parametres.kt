package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.ParametresBinding

class Parametres : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ParametresBinding.inflate(inflater, container, false)
        binding.parametres = this
        return binding.root
    }

    fun onClickApparence(view: View){
        findNavController().navigate(R.id.parametres_to_apparence)
    }

}
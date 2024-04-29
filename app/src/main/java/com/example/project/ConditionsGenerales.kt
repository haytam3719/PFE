package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.ConditionsGeneralesBinding


class ConditionsGenerales : Fragment(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ConditionsGeneralesBinding.inflate(inflater, container, false)
        val button = binding.button

        button.setOnClickListener{
            findNavController().navigate(R.id.conditions_generales_to_contrat)
        }
        return binding.root


    }




    }

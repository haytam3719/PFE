package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.project.databinding.ApparenceBinding
import com.google.android.material.appbar.MaterialToolbar

class Apparence : Fragment(){

    private var _binding: ApparenceBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.checkbox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkbox2.isChecked = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.checkbox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.checkbox1.isChecked = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }



    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ApparenceBinding.inflate(inflater, container, false)
        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            android.content.res.Configuration.UI_MODE_NIGHT_NO -> {
                binding.checkbox1.isChecked = true
                binding.checkbox2.isChecked = false
            }
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                binding.checkbox1.isChecked = false
                binding.checkbox2.isChecked = true
            }
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
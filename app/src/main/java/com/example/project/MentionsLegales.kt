package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.databinding.MentionsLegalesBinding
import com.google.android.material.appbar.MaterialToolbar

class MentionsLegales : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MentionsLegalesBinding.inflate(inflater, container, false)
        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        return binding.root
    }
}
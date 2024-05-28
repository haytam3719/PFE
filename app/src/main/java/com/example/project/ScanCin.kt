package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.FragmentCinScanBinding
import com.example.project.viewmodels.ScanCinViewModel
import com.google.android.material.appbar.MaterialToolbar

class ScanCin : Fragment() {
    private val scanCinViewModel: ScanCinViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanCinViewModel.navigateToCamera.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navigate when event is triggered
                findNavController().navigate(R.id.sacncin_to_cameracin)

                // Reset the navigation event after navigation
                scanCinViewModel.onNavigationComplete()
            }
        })


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCinScanBinding.inflate(inflater, container, false)

        binding.scanCinViewModel = scanCinViewModel

        binding.lifecycleOwner = viewLifecycleOwner

        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        val lottieAnimationView = binding.animationView
        lottieAnimationView.setAnimation("identity_card_scan.json")
        lottieAnimationView.loop(true)
        lottieAnimationView.speed = 1f

        lottieAnimationView.playAnimation()

        return binding.root


    }}





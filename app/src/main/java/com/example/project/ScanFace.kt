package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.FragmentFaceScanBinding
import com.example.project.viewmodels.ScanFaceViewModel

class ScanFace : Fragment() {
    private val scanFaceViewModel: ScanFaceViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanFaceViewModel.navigateToCamera.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navigate when event is triggered
                findNavController().navigate(R.id.scanface_to_cameraface)

                // Reset the navigation event after navigation
                scanFaceViewModel.onNavigationComplete()
            }
        })


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFaceScanBinding.inflate(inflater, container, false)

        binding.scanFaceViewModel = scanFaceViewModel

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root


    }}





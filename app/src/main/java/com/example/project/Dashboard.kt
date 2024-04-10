package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.DashboardBinding
import com.example.project.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Dashboard : Fragment(){
    private val dashboardViewModel by viewModels<DashboardViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel.navigateToVirement.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(R.id.dashboard_to_virement)

                    // Reset the navigation event after navigation
                    dashboardViewModel.onNavigationComplete()
                }
            })


        dashboardViewModel.navigateToCreateAccount.observe(
            viewLifecycleOwner,
            Observer { shouldNavigate ->
                if (shouldNavigate) {
                    // Navigate when event is triggered
                    findNavController().navigate(R.id.dashboard_to_createaccount)

                    // Reset the navigation event after navigation
                    dashboardViewModel.onNavigationCompleteCreateAccount()
                }
            })



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DashboardBinding.inflate(inflater, container, false)
        binding.dashboardViewModel=dashboardViewModel


        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root

    }

}
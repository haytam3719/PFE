package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.DashboardBinding
import com.example.project.models.DeviceInfo
import com.example.project.viewmodels.AuthViewModel
import com.example.project.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Dashboard : Fragment(){
    private val dashboardViewModel by viewModels<DashboardViewModel>()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var deviceInfo: DeviceInfo
    private var _binding: DashboardBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.navigateToPayment.observe(
            viewLifecycleOwner,
            Observer{shouldnavigate ->
                if(shouldnavigate){
                    findNavController().navigate(R.id.dashboard_to_payment)

                    dashboardViewModel.onNavigationCompletePayment()
                }
            }
        )

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
                    findNavController().navigate(R.id.dashboard_to_createaccount)

                    dashboardViewModel.onNavigationCompleteCreateAccount()
                }
            })


        deviceInfo = DeviceInfo.fromContext(requireContext())
        dashboardViewModel.deviceInfoList.observe(viewLifecycleOwner, Observer { deviceList ->
            if (deviceList != null) {
                Log.d("Fetched devices list", deviceList.toString())
                val connectedDevice = dashboardViewModel.getDeviceInfo(requireContext())

                val isTrustedDevice = deviceList.any { deviceInfo ->
                    deviceInfo.deviceModel == connectedDevice.deviceModel &&
                            deviceInfo.androidVersion == connectedDevice.androidVersion &&
                            deviceInfo.networkOperatorName == connectedDevice.networkOperatorName
                }

                if (isTrustedDevice) {
                    Log.d("Connected Device", "Trusted Device")
                    binding.contentLayout.visibility = View.VISIBLE

                } else {
                    Log.e("Connected Device", "Non-trusted Device")
                    showSuspiciousConnectionPopup()
                    binding.contentLayout.visibility = View.VISIBLE
                }
            } else {
                Log.d("Dashboard", "Device list is null or empty")
            }
        })

        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        dashboardViewModel.fetchDeviceListByClientUid(userUid)

    }


    fun showSuspiciousConnectionPopup() {
        val context = requireContext()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Connexion suspecte")
        builder.setMessage("L'appareil actuel ne peut pas être trouvé dans la liste des appareils de confiance associés à l'utilisateur.")
        builder.setCancelable(false)

        builder.setPositiveButton("Faire confiance") { dialog, which ->
            val bundle = Bundle().apply {
                putBoolean("shouldTrustDevice", true)
            }
            findNavController().navigate(R.id.dashboard_to_verifyCredentials,bundle)
        }

        builder.setNegativeButton("Se déconnecter") { dialog, which ->
            authViewModel.signOut()
            findNavController().navigate(R.id.dashboard_to_mainactplaceholder)
        }

        builder.create().show()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardBinding.inflate(inflater, container, false)
        binding.dashboardViewModel=dashboardViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.contentLayout.visibility = View.GONE


        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
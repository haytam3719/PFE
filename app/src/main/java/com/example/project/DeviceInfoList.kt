package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.adapters.DeviceInfoAdapter
import com.example.project.databinding.DeviceInfoListBinding
import com.example.project.viewmodels.DashboardViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceInfoList : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceInfoAdapter
    private val dashboardViewModel:DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DeviceInfoListBinding.inflate(inflater, container, false)
        val topAppBar: MaterialToolbar = binding.topAppBar

        dashboardViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })



        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        recyclerView = binding.recyclerView
        adapter = DeviceInfoAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dashboardViewModel.deviceInfoList.observe(viewLifecycleOwner, Observer { deviceList ->
            if (deviceList != null) {
                adapter.updateDevices(deviceList)
            }
        })

        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        dashboardViewModel.fetchDeviceListByClientUid(userUid)


        return binding.root
    }



}
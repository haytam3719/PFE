package com.example.project.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.DeviceInfoItemBinding
import com.example.project.models.DeviceInfo

class DeviceInfoAdapter(private var devices: List<DeviceInfo>) : RecyclerView.Adapter<DeviceInfoAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(val binding: DeviceInfoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = DeviceInfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        Log.d("DeviceInfoAdapter", "Binding view holder at position: $position")

        val device = devices[position]
        with(holder.binding) {
            textViewDeviceModel.text = "Modèle: ${device.deviceModel}"
            textViewAndroidVersion.text = "Version Android: ${device.androidVersion}"
            textViewNetworkOperatorName.text = "Opérateur:\n${device.networkOperatorName}"
        }
    }

    override fun getItemCount(): Int {
        Log.d("DeviceInfoAdapter", "Total items: ${devices.size}")
        return devices.size
    }
    fun updateDevices(newDevices: List<DeviceInfo>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}

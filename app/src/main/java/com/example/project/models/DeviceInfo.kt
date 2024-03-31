package com.example.project.models

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.text.format.Formatter

data class DeviceInfo(
    val ipAddress: String,
    val deviceModel: String,
    val androidVersion: String,
    val networkOperatorName: String
) {
    companion object {
        fun fromContext(context: Context): DeviceInfo {
            val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val ipAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            val deviceModel = Build.MODEL
            val androidVersion = Build.VERSION.RELEASE

            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkOperatorName = telephonyManager.networkOperatorName

            return DeviceInfo(ipAddress, deviceModel, androidVersion, networkOperatorName )
        }
    }
}

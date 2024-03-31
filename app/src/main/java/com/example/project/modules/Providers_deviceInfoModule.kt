package com.example.project.modules

import com.example.project.models.DeviceInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceInfoModeule {

    @Provides
    @Singleton
    fun provideDeviceInfo(): DeviceInfo {
        // Initialize and return DeviceInfo object
        return DeviceInfo("","","","")
    }
}
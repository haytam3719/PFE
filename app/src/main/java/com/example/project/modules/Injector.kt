package com.example.project.modules

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.example.project.CollectionInfosFragment
import com.example.project.MainActPlaceHolder
import com.example.project.MainActivity
import com.example.project.Virement
import com.example.project.models.CompteImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.dagger.Component
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
@Component(modules = [AuthModule::class])
class AppComponent:Application(),  CameraXConfig.Provider{
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    fun inject(activity: MainActivity) {}
    fun inject(fragment: MainActPlaceHolder) {}
    fun inject(fragment: CollectionInfosFragment){}

    fun inject(compteImpl: CompteImpl){}
    fun inject(virement: Virement){}

}

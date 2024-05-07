package com.example.project.modules

import android.app.Application
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.example.project.CollectionInfosFragment
import com.example.project.MainActPlaceHolder
import com.example.project.MainActivity
import com.example.project.Payment
import com.example.project.Virement
import com.example.project.models.CompteImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.dagger.Component
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
@HiltAndroidApp
@Singleton
@Component(modules = [AuthModule::class])
class AppComponent:Application(),  CameraXConfig.Provider{
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM Token", token)
                // Save the FCM token to server for sending notifications
            }
        }

        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }
            })

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }

            // Optional: Log a message to indicate that SSL validation is disabled
            Log.d("SSL Validation", "SSL certificate validation is disabled (NOT recommended for production use)")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    fun inject(activity: MainActivity) {}
    fun inject(fragment: MainActPlaceHolder) {}
    fun inject(fragment: CollectionInfosFragment){}

    fun inject(compteImpl: CompteImpl){}
    fun inject(virement: Virement){}
    fun inject(fragment: Payment) {}

}

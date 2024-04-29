package com.example.project

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SplashScreenFragment : Fragment() {

    private lateinit var progressBar: ProgressBar

    private val SPLASH_TIMEOUT: Long = 3000
    private val IMAGE_DELAY: Long = 3000
    private val PROGRESS_BAR_DELAY: Long = 6000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        val logoImageView: ImageView = view.findViewById(R.id.logo)
        val croireEnVousImageView: ImageView = view.findViewById(R.id.coire_en_vous)
        progressBar = view.findViewById(R.id.progressBar)

        // Start logo animation with delay
        Handler(Looper.getMainLooper()).postDelayed({
            logoImageView.visibility = View.VISIBLE
            logoImageView.startAnimation(fadeInAnimation)
        }, SPLASH_TIMEOUT)

        // Start "croire_en_vous" ImageView animation with delay
        Handler(Looper.getMainLooper()).postDelayed({
            croireEnVousImageView.visibility = View.VISIBLE
            croireEnVousImageView.startAnimation(fadeInAnimation)
        }, SPLASH_TIMEOUT + IMAGE_DELAY)

        // Start CircularProgressIndicator animation with delay
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.VISIBLE
            simulateProgress()
        }, SPLASH_TIMEOUT + PROGRESS_BAR_DELAY)

        // Navigate to the next destination after SPLASH_TIMEOUT + PROGRESS_BAR_DELAY milliseconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Perform navigation with animation
            findNavController().navigate(R.id.splashscreen_to_mainactplace)
        }, SPLASH_TIMEOUT + PROGRESS_BAR_DELAY + 100) // Adjust the delay time as needed
    }

    private fun simulateProgress() {
        progressBar.progress = 0

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                progressBar.progress += 10

                if (progressBar.progress < 100) {
                    handler.postDelayed(this, 100)
                }
            }
        }, 100)
    }
}

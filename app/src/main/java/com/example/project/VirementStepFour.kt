package com.example.project

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.project.databinding.VirementStepFourBinding
import com.example.project.models.CircularProgressView
import com.example.project.viewmodels.ProgressBarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VirementStepFour : Fragment() {
    private val progressViewModel: ProgressBarViewModel by activityViewModels()

    private var _binding: VirementStepFourBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VirementStepFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val circularProgressBar = binding.circularProgressBar
        circularProgressBar.progress = 75
        Handler(Looper.getMainLooper()).postDelayed({
            animateProgress(circularProgressBar, 100) {
                progressViewModel.setProgress(100)
            }
        }, 500)
    }

    private fun animateProgress(progressView: CircularProgressView, toProgress: Int, onEnd: () -> Unit) {
        val animator = ValueAnimator.ofInt(progressView.progress, toProgress)
        animator.duration = 500
        animator.addUpdateListener { animation ->
            progressView.updateProgress(animation.animatedValue as Int)
        }
        animator.addListener(onEnd = { onEnd() })
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

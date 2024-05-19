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
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.VirementStepThreeBinding
import com.example.project.models.CircularProgressView
import com.example.project.viewmodels.ProgressBarViewModel
import com.example.project.viewmodels.VirementUpdatedViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VirementStepThree : Fragment() {
    private val progressViewModel:ProgressBarViewModel by activityViewModels()
    private val sharedViewModel:VirementUpdatedViewModel by activityViewModels()
    private var _binding: VirementStepThreeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VirementStepThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val circularProgressBar = binding.circularProgressBar
        circularProgressBar.progress = 50
        Handler(Looper.getMainLooper()).postDelayed({
            animateProgress(circularProgressBar, 75) {
                progressViewModel.setProgress(75)
            }
        }, 500)
        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.virementStepThree_to_virementStepFour)
        }

        binding.nextButton.setOnClickListener {
            val isPlanned = binding.radioGroup.checkedRadioButtonId == R.id.planifier_virement
            val selectedDate = if (isPlanned) binding.dateSelectedEditText.text.toString() else null

            sharedViewModel.setTransferPlanned(isPlanned)
            sharedViewModel.setSelectedDate(selectedDate)

            findNavController().navigate(R.id.virementStepThree_to_virementStepFour)
        }

        setupDatePicker()
    }

    private fun setupDatePicker() {
        binding.datePickerTextInputLayout.setEndIconOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selectionner une date")
                .build()

            datePicker.show(childFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val dateFormatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val dateString = dateFormatter.format(java.util.Date(selection))
                binding.dateSelectedEditText.setText(dateString)
            }
        }
    }

    private fun animateProgress(progressView: CircularProgressView, toProgress: Int, onEnd: () -> Unit) {
        val animator = ValueAnimator.ofInt(progressView.progress, toProgress)
        animator.duration = 500 // 0.5 second for smooth animation
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

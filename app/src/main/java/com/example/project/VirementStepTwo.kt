package com.example.project

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.animation.addListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.VirementStepTwoBinding
import com.example.project.models.CircularProgressView
import com.example.project.viewmodels.ProgressBarViewModel
import com.example.project.viewmodels.VirementUpdatedViewModel
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VirementStepTwo : Fragment() {
    private val progressViewModel: ProgressBarViewModel by activityViewModels()
    private val sharedViewModel: VirementUpdatedViewModel by activityViewModels()

    private lateinit var gestureDetector: GestureDetector

    private var _binding: VirementStepTwoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VirementStepTwoBinding.inflate(inflater, container, false)
        progressViewModel.setProgress(25)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topAppBar: MaterialToolbar = binding.topAppBar

        topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        val circularProgressBar = binding.circularProgressBar
        circularProgressBar.progress = 25
        Handler(Looper.getMainLooper()).postDelayed({
            animateProgress(circularProgressBar, 50) {
                progressViewModel.setProgress(50)
            }
        }, 500)


        binding.nextButton.setOnClickListener {
            val motif = binding.textInputMotif.editText?.text?.toString()
            val amount = binding.textInputAmount.editText?.text?.toString()?.toIntOrNull()

            if (motif.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Le champ motif est obligatoire", Toast.LENGTH_LONG).show()
            } else if (amount == null) {
                Toast.makeText(requireContext(), "Le champ montant est obligatoire", Toast.LENGTH_LONG).show()
            } else {
                sharedViewModel.setMotif(motif)
                sharedViewModel.setAmount(amount)

                if(amount > sharedViewModel.selectedAccount.value?.balance!!.toDouble()){
                    Toast.makeText(requireContext(), "Votre solde est insuffisant pour effectuer l'opération courante", Toast.LENGTH_LONG).show()
                }else {
                    findNavController().navigate(R.id.virementStepTwo_to_virementStepThree)
                }
            }
        }


        setupSeekBar()
        setupEditText()
    }

    private fun setupSeekBar() {
        binding.seekBarAmount.max = 50000
        binding.seekBarAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textInputAmount.editText?.setText(progress.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun setupEditText() {
        binding.textInputAmount.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.toIntOrNull()?.let { value ->
                    if (value <= binding.seekBarAmount.max && value >= 0) {
                        binding.seekBarAmount.progress = value
                    }
                }
            }
        })
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

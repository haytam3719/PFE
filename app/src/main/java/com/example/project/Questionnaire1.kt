package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.example.project.databinding.Questionnaire1Binding
import com.example.project.viewmodels.QuestionnaireViewModel

class Questionnaire1 : Fragment() {
    private val questionnaireViewModel: QuestionnaireViewModel by activityViewModels()
    private var _binding: Questionnaire1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Questionnaire1Binding.inflate(inflater, container, false)
        val rootView = binding.root
        val buttonOui = binding.customRadioButton
        val buttonNon = binding.customRadioButton1

        buttonOui.setOnClickListener {
            // Update ViewModel with true for "Oui"
            questionnaireViewModel.updateQuestion1(true)
        }

        buttonNon.setOnClickListener {
            // Update ViewModel with false for "Non"
            questionnaireViewModel.updateQuestion1(false)
        }

        val confirmationButton = binding.button
        confirmationButton.setOnClickListener {
            if(!(binding.customRadioButton.isChecked) && !(binding.customRadioButton1.isChecked)){
                Toast.makeText(requireContext(),"Veuillez sélectionner au moins une option",Toast.LENGTH_LONG).show()
            }
            else {
                // Log the Questionnaire object
                questionnaireViewModel.questionnaire.observe(viewLifecycleOwner) { questionnaire ->
                    Log.d("Questionnaire1", "Questionnaire object: $questionnaire")
                }
                onNextButtonClick()
            }
        }


        binding.customRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.customRadioButton1.isChecked = false
            }
        }

        binding.customRadioButton1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.customRadioButton.isChecked = false
            }
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onNextButtonClick() {
        val viewPager = requireActivity().findViewById<ViewPager>(R.id.view_pager)
        Log.d("Questionnaire1", "ViewPager current item: ${viewPager.currentItem}, Adapter count: ${viewPager.adapter?.count}")

        // Ensure that the next item is within the bounds of the ViewPager
        if (viewPager.currentItem + 1 < (viewPager.adapter?.count ?: 0)) {
            // Navigate to the next fragment in the ViewPager
            viewPager.currentItem = viewPager.currentItem + 1
        } else {
            Log.d("Questionnaire1", "Reached the end of ViewPager")
        }
    }
}

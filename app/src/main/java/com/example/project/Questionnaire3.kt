package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.example.project.databinding.Questionnaire3Binding
import com.example.project.viewmodels.QuestionnaireViewModel

class Questionnaire3 : Fragment() {
    private var selectedResponse: String? = null
    private val questionnaireViewModel: QuestionnaireViewModel by activityViewModels()
    private var _binding: Questionnaire3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Questionnaire3Binding.inflate(inflater, container, false)
        val rootView = binding.root
        val buttonOui = binding.customRadioButton
        val buttonNon = binding.customRadioButton1
        val buttonNext = binding.button

        buttonOui.setOnClickListener {
            // Uncheck the other RadioButton
            buttonNon.isChecked = false
            // Store the selected response
            selectedResponse = buttonOui.text.toString()
            questionnaireViewModel.updateQuestion3(true)
        }

        buttonNon.setOnClickListener {
            // Uncheck the other RadioButton
            buttonOui.isChecked = false
            // Store the selected response
            selectedResponse = buttonNon.text.toString()
        }

        buttonNext.setOnClickListener {
            if(!(binding.customRadioButton.isChecked) && !(binding.customRadioButton1.isChecked)){
                Toast.makeText(requireContext(),"Veuillez sélectionner au moins une option", Toast.LENGTH_LONG).show()
            }
            else if (selectedResponse == "Oui") {
                questionnaireViewModel.updateQuestion3(true)
            } else {
                questionnaireViewModel.updateQuestion3(false)
            }

            onNextButtonClick()
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
        // Navigate to the next fragment in the ViewPager
        viewPager.currentItem = viewPager.currentItem + 1
    }
}

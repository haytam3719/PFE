package com.example.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.Questionnaire6Binding
import com.example.project.viewmodels.QuestionnaireViewModel

class Questionnaire6 : Fragment() {
    private var selectedResponse: String? = null
    private val questionnaireViewModel: QuestionnaireViewModel by activityViewModels()

    private var _binding: Questionnaire6Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Questionnaire6Binding.inflate(inflater, container, false)
        val rootView = binding.root
        val buttonNext = binding.button
        val buttonOui = binding.customRadioButton
        val buttonNon = binding.customRadioButton1

        questionnaireViewModel.questionnaire.observe(viewLifecycleOwner) { questionnaire ->
            // Log the entire questionnaire object when it changes
            Log.e("QuestionnaireData", "Questionnaire: $questionnaire")
        }

        buttonOui.setOnClickListener {
            // Uncheck the other RadioButton
            buttonNon.isChecked = false
            // Store the selected response
            selectedResponse = buttonOui.text.toString()
            questionnaireViewModel.updateQuestion6(true)
        }

        buttonNon.setOnClickListener {
            // Uncheck the other RadioButton
            buttonOui.isChecked = false
            // Store the selected response
            selectedResponse = buttonNon.text.toString()
        }

        buttonNext.setOnClickListener {
            Log.e("Debug6", "$selectedResponse")
            if (selectedResponse == "Oui") {
                questionnaireViewModel.updateQuestion6(true)
                findNavController().navigate(R.id.viewPager_to_conditiongene)
            } else {
                questionnaireViewModel.updateQuestion6(false)
                findNavController().navigate(R.id.viewPager_to_conditiongene)
            }
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

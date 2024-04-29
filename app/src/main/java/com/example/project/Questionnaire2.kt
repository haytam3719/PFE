package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.example.project.databinding.Questionnaire2Binding
import com.example.project.viewmodels.QuestionnaireViewModel

class Questionnaire2 : Fragment() {
    private var selectedResponse: String? = null
    private var _binding: Questionnaire2Binding? = null
    private val questionnaireViewModel: QuestionnaireViewModel by activityViewModels()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Questionnaire2Binding.inflate(inflater, container, false)
        val rootView = binding.root

        val buttonOui = binding.customRadioButton
        val buttonNon = binding.customRadioButton1
        val buttonNext = binding.button

        buttonOui.setOnClickListener {
            // Uncheck the other RadioButton
            buttonNon.isChecked = false
            // Store the selected response
            selectedResponse = buttonOui.text.toString()
        }

        buttonNon.setOnClickListener {
            // Uncheck the other RadioButton
            buttonOui.isChecked = false
            // Store the selected response
            selectedResponse = buttonNon.text.toString()
            questionnaireViewModel.updateQuestion2(true)
        }

        buttonNext.setOnClickListener {
            if (selectedResponse == "Oui") {
                questionnaireViewModel.updateQuestion2(true)
            } else {
                questionnaireViewModel.updateQuestion2(false)
            }

            onNextButtonClick()
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

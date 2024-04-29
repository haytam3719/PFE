package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.models.Questionnaire

class QuestionnaireViewModel : ViewModel() {
    private val _questionnaire = MutableLiveData<Questionnaire>()
    val questionnaire: LiveData<Questionnaire>
        get() = _questionnaire

    fun updateQuestion1(answer: Boolean) {
        val updatedQuestionnaire = _questionnaire.value ?: Questionnaire()
        updatedQuestionnaire.question1 = answer
        _questionnaire.value = updatedQuestionnaire
    }


    fun updateQuestion2(answer: Boolean) {
        val updatedQuestionnaire = _questionnaire.value ?: Questionnaire()
        updatedQuestionnaire.question2 = answer
        _questionnaire.value = updatedQuestionnaire
    }


    fun updateQuestion3(answer: Boolean) {
        val updatedQuestionnaire = _questionnaire.value ?: Questionnaire()
        updatedQuestionnaire.question3 = answer
        _questionnaire.value = updatedQuestionnaire
    }


    fun updateQuestion4(answer: Boolean) {
        val updatedQuestionnaire = _questionnaire.value ?: Questionnaire()
        updatedQuestionnaire.question4 = answer
        _questionnaire.value = updatedQuestionnaire
    }


    fun updateQuestion5(answer: Boolean) {
        val updatedQuestionnaire = _questionnaire.value ?: Questionnaire()
        updatedQuestionnaire.question5 = answer
        _questionnaire.value = updatedQuestionnaire
    }


    fun updateQuestion6(answer: Boolean) {
        val updatedQuestionnaire = _questionnaire.value ?: Questionnaire()
        updatedQuestionnaire.question6 = answer
        _questionnaire.value = updatedQuestionnaire
    }



}

package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProgressBarViewModel @Inject constructor() : ViewModel() {
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress

    init {
        _progress.value = 0
    }

    fun setProgress(newProgress: Int) {
        _progress.value = newProgress
    }

    fun incrementProgress(increment: Int) {
        val current = _progress.value ?: 0
        setProgress((current + increment) % 100)
    }

    fun resetProgress() {
        _progress.value = 0
    }
}

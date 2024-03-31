package com.example.project.models

sealed class SignUpState {
    object Initial : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}
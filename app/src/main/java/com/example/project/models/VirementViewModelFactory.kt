package com.example.project.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project.prototype.AccountRepository
import com.example.project.viewmodels.VirementViewModel

class VirementViewModelFactory(private val virement: Virement,private val accountRepository: AccountRepository) : ViewModelProvider.Factory {
     override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VirementViewModel::class.java)) {
            return VirementViewModel(virement, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

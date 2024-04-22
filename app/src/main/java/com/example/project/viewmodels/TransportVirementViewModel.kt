package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.models.Virement

class TransportVirementViewModel : ViewModel() {
    private val _virement = MutableLiveData<Virement>()
    val virement: LiveData<Virement> = _virement

    fun setVirement(virement: Virement) {
        _virement.value = virement
    }
}
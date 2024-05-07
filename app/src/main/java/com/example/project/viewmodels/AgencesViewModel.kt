package com.example.project.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.models.Agence
import com.example.project.models.Type
import com.example.project.prototype.AgencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AgencesViewModel @Inject constructor(private val agencesRepository: AgencesRepository) : ViewModel() {
        private val _agences = MutableLiveData<List<Agence>>()
        val agences: LiveData<List<Agence>> get() = _agences
        private val _types = MutableLiveData<List<Type>>()
        val types: LiveData<List<Type>> get() = _types

        init {
            viewModelScope.launch {
                loadAgences(0.0, 0.0, "getAgencesWafacashNear")
                loadTypes()
            }
        }

        fun loadAgences(latitude: Double, longitude: Double, action: String) {
            viewModelScope.launch {
                _agences.postValue(agencesRepository.getAgencesNear(latitude, longitude, action))
            }
        }

        private fun loadTypes() {
            viewModelScope.launch {
                _types.postValue(agencesRepository.getTypes())
            }
        }

        fun loadAgencesWithinRadius(latitude: Double, longitude: Double, radius: Double, action: String) {
            viewModelScope.launch {
                _agences.postValue(agencesRepository.getAgencesWithinRadius(latitude, longitude, radius, action))
            }
        }
    }





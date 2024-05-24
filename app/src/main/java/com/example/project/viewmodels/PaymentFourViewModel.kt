package com.example.project.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.models.CarteImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentFourViewModel @Inject constructor() : ViewModel() {

    private val _selectedCard = MutableLiveData<CarteImpl>()
    val selectedCard: LiveData<CarteImpl> = _selectedCard

    fun selectCard(card: CarteImpl) {
        _selectedCard.value = card
    }
}

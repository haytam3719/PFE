package com.example.project.prototype

import com.example.project.models.CarteImpl
import com.example.project.repositories.CarteRepositoryImpl

interface CarteRepository {
    fun ajouterCarte(carte: CarteImpl, callback: CarteRepositoryImpl.CarteRepositoryCallback<Unit>)
    fun supprimerCarte(id: String, callback: CarteRepositoryImpl.CarteRepositoryCallback<Unit>)
    fun modifierCarte(carte: CarteImpl, callback: CarteRepositoryImpl.CarteRepositoryCallback<Unit>)
    fun getCarte(id: String, callback: CarteRepositoryImpl.CarteRepositoryCallback<CarteImpl>)
    fun getAllCartes(callback: CarteRepositoryImpl.CarteRepositoryCallback<List<CarteImpl>>)
    fun getCartesByIdProprietaire(idProprietaire: String, callback: CarteRepositoryImpl.CarteRepositoryCallback<List<CarteImpl>>)
    }

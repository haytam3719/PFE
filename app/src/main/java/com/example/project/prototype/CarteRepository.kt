package com.example.project.prototype

import com.google.firebase.database.ValueEventListener

interface CarteRepository {
    fun ajouterCarte(carte: Carte)
    fun supprimerCarte(id: String)
    fun modifierCarte(carte: Carte)
    fun getCarte(id: String, valueEventListener: ValueEventListener)
    fun getAllCartes(valueEventListener: ValueEventListener)
    fun getCartesByIdProprietaire(id_proprietaire: String, valueEventListener: ValueEventListener)
}

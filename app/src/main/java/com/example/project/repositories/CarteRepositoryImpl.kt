package com.example.project.repositories

import com.example.project.prototype.Carte
import com.example.project.prototype.CarteRepository
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class CarteRepositoryImpl @Inject constructor() : CarteRepository {
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("cartes")

    override fun ajouterCarte(carte: Carte) {
        val id = databaseReference.push().key
        id?.let {
            carte.idCarte = it
            databaseReference.child(it).setValue(carte)
        }
    }

    override fun supprimerCarte(id: String) {
        databaseReference.child(id).removeValue()
    }

    override fun modifierCarte(carte: Carte) {
        databaseReference.child(carte.idCarte).setValue(carte)
    }

    override fun getCarte(id: String, valueEventListener: ValueEventListener) {
        databaseReference.child(id).addListenerForSingleValueEvent(valueEventListener)
    }

    override fun getAllCartes(valueEventListener: ValueEventListener) {
        databaseReference.addListenerForSingleValueEvent(valueEventListener)
    }

    override fun getCartesByIdProprietaire(id_proprietaire: String, valueEventListener: ValueEventListener) {
        val query = databaseReference.orderByChild("id_proprietaire_carte").equalTo(id_proprietaire)
        query.addListenerForSingleValueEvent(valueEventListener)
    }
}

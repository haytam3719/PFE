package com.example.project.repositories

import android.util.Log
import com.example.project.models.CarteImpl
import com.example.project.prototype.CarteRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class CarteRepositoryImpl @Inject constructor() : CarteRepository {
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("cartes")

    interface CarteRepositoryCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: DatabaseError)
    }

    override fun ajouterCarte(carte: CarteImpl, callback: CarteRepositoryCallback<Unit>) {
        val id = databaseReference.push().key ?: ""
        Log.d("CarteRepo", "Attempting to add carte with generated ID: $id")

        if (id.isNotEmpty()) {
            carte.idCarte = id
            databaseReference.child(id).setValue(carte)
                .addOnSuccessListener {
                    Log.d("CarteRepo", "Successfully added card with ID: ${carte.idCarte}")
                    println("Added a card with ID: ${carte.idCarte}")
                    callback.onSuccess(Unit)
                }
                .addOnFailureListener {
                    Log.e("CarteRepo", "Failed to add card", it)
                    callback.onError(DatabaseError.fromException(it))
                }
        } else {
            Log.e("CarteRepo", "Failed to generate a key for new card")
            callback.onError(DatabaseError.fromException(Exception("Failed to generate a key")))
        }
    }

    override fun supprimerCarte(id: String, callback: CarteRepositoryCallback<Unit>) {
        databaseReference.child(id).removeValue()
            .addOnSuccessListener {
                callback.onSuccess(Unit)  // Pass Unit here
            }
            .addOnFailureListener {
                callback.onError(DatabaseError.fromException(it))
            }
    }

    override fun modifierCarte(carte: CarteImpl, callback: CarteRepositoryCallback<Unit>) {
        databaseReference.child(carte.idCarte).setValue(carte)
            .addOnSuccessListener {
                callback.onSuccess(Unit)
            }
            .addOnFailureListener {
                callback.onError(DatabaseError.fromException(it))
            }
    }


    override fun getCarte(id: String, callback: CarteRepositoryCallback<CarteImpl>) {
        databaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val carte = snapshot.getValue(CarteImpl::class.java)
                if (carte != null) {
                    callback.onSuccess(carte)
                } else {
                    callback.onError(DatabaseError.fromException(Exception("Carte not found")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error)
            }
        })
    }

    override fun getAllCartes(callback: CarteRepositoryCallback<List<CarteImpl>>) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartes = mutableListOf<CarteImpl>()
                snapshot.children.mapNotNullTo(cartes) { it.getValue(CarteImpl::class.java) }
                callback.onSuccess(cartes)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error)
            }
        })
    }

    override fun getCartesByIdProprietaire(idProprietaire: String, callback: CarteRepositoryCallback<List<CarteImpl>>) {
        val query = databaseReference.orderByChild("id_proprietaire_carte").equalTo(idProprietaire)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartes = mutableListOf<CarteImpl>()
                snapshot.children.mapNotNullTo(cartes){ it.getValue(CarteImpl::class.java) }
                callback.onSuccess(cartes)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error)
            }
        })
    }

    fun modifierSecurityCode(id: String, newSecurityCode: String, callback: CarteRepositoryCallback<Unit>) {
        val cardRef = databaseReference.child(id)

        cardRef.child("codeSecurite").setValue(newSecurityCode).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback.onSuccess(Unit)
            } else {
                callback.onError(task.exception as DatabaseError)
            }
        }
    }
}

package com.example.project.models

data class Agence(
    val id: String,
    val ville: String,
    val type: String,
    val nom: String,
    val horaire1: String,
    val horaire2: String,
    val horaire3: String,
    val horaire4: String,
    val horaire5: String,
    val horaire6: String,
    val adresse: String,
    val telephone1: String,
    val telephone2: String,
    val fax: String,
    val latitude: String,
    val longitude: String,
    val distance: String
)


data class AgenceResponse(
    val agence: List<Agence>
)

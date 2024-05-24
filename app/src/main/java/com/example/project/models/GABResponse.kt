package com.example.project.models

data class GAB(
    val id: String,
    val ville: String,
    val type: String,
    val nom: String,
    val adresse: String,
    val latitude: String,
    val longitude: String,
    val distance: String
)

data class GABResponse(
    val gab: List<GAB>
)

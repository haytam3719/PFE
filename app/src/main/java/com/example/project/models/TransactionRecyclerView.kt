package com.example.project.models

data class Transaction(
    val id: String,
    val montant: Double,
    val date: String,
    val typeTransaction: String,
    val compteEmet: String,
    val compteBenef: String
)

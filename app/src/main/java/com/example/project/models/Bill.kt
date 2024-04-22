package com.example.project.models

data class Bill(
    val id: String,
    val amount: Double,
    val due_date: String,
    val number: String
)

data class PaymentDetails(
    val billId: String,
    val amount: Double,
)

package com.example.project.models

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("due_date") val due_date: String,
    @SerializedName("number") val number: String
)

data class PaymentResponse(val check:Boolean)

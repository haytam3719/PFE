package com.example.project.models

import com.google.gson.annotations.SerializedName

data class Subtype(
    val id: String,
    val nom: String
)

data class Type(
    val nom: String,
    val liste: List<Subtype>
)

data class TypeResponse(
    @SerializedName("types")
    val types: List<Type>
)

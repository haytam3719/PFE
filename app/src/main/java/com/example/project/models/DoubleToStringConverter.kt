package com.example.project.models

import androidx.databinding.InverseMethod

object DoubleToStringConverter {

    @InverseMethod("stringToDouble")
    @JvmStatic
    fun doubleToString(value: Double?): String? {
        return try {
            value?.toString()
        } catch (e: Exception) {
            // Handle conversion error here, such as logging or returning a default value
            e.printStackTrace()
            "0.0"
        }
    }

    @JvmStatic
    fun stringToDouble(value: String?): Double? {
        return try {
            value?.toDouble()
        } catch (e: NumberFormatException) {
            // Handle conversion error here, such as logging or returning a default value
            e.printStackTrace()
            0.0
        }
    }
}

package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.databinding.AttijariSecureBinding
import com.google.firebase.database.FirebaseDatabase

class AttijariSecure : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AttijariSecureBinding.inflate(inflater, container, false)

        testFirebaseDatabase()

        return binding.root
    }

    fun testFirebaseDatabase() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("testNode")

        // Attempt to write a value to the database
        myRef.setValue("Hello Firebase").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Data was written successfully to Firebase.")
                // Following the successful write, try to read the value back
                myRef.get().addOnCompleteListener { readTask ->
                    if (readTask.isSuccessful) {
                        val value = readTask.result?.value
                        println("Successfully read from Firebase: $value")
                    } else {
                        println("Failed to read from Firebase: ${readTask.exception}")
                    }
                }
            } else {
                println("Failed to write to Firebase: ${task.exception}")
            }
        }
    }

}
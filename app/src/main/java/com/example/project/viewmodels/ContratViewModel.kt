package com.example.project.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class ContratViewModel : ViewModel() {

    private val _data = MutableLiveData<String>()
    val dataFromFragment: LiveData<String> = _data
    private lateinit var numCin:String

    fun setData(newData: String) {
        _data.value = newData
    }

    init {
        // Observe changes to the LiveData object
        dataFromFragment.observeForever { newData ->
            numCin = newData
            Log.d("CameraFaceViewModel", "Data from fragment CameraFace: $newData")
            Log.d("CameraFaceViewModel", "numCin initialized: $numCin")
        }
    }


    private val storage = Firebase.storage
    private val storageRef = storage.reference

    fun uploadSignatureImage(numCin: String, signatureBitmap: Bitmap) {
        val imageRef = storageRef.child("clients/$numCin/signature")

        // Convert bitmap to byte array
        val data = convertBitmapToByteArray(signatureBitmap)

        // Upload byte array to Firebase Storage
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Signature image uploaded successfully
            Log.d("Upload", "Signature uploaded successfully")
        }.addOnFailureListener { exception ->
            // Handle any errors that occur during the upload process
            Log.e("Upload", "Error uploading signature", exception)
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }
}


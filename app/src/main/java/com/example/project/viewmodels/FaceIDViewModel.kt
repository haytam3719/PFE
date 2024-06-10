package com.example.project.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.repositories.FaceIdRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FaceIDViewModel @Inject constructor(private val faceIdRepository: FaceIdRepository): ViewModel() {

    private val _uploadStatus = MutableLiveData<String?>()
    val uploadStatus: MutableLiveData<String?> get() = _uploadStatus

    private val _finalDecision = MutableLiveData<String?>()
    val finalDecision: MutableLiveData<String?> get() = _finalDecision

    fun uploadReferenceImage(filePath: String) {
        faceIdRepository.uploadReferenceImage(filePath) { success, message ->
            _uploadStatus.postValue(message)
        }
    }

    fun getFinalDecision() {
        faceIdRepository.getFinalDecision { success, message ->
            _finalDecision.postValue(message)
        }
    }



    fun downloadAndUploadImage(context: Context, firebaseImagePath: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child(firebaseImagePath)
        val localFile = File.createTempFile("reference_image", "jpg", context.cacheDir)

        storageRef.getFile(localFile).addOnSuccessListener {
            Log.d("FaceIDViewModel", "Image downloaded successfully: ${localFile.absolutePath}")
            uploadReferenceImage(localFile.absolutePath)
        }.addOnFailureListener { exception ->
            Log.e("FaceIDViewModel", "Failed to download image: ${exception.message}")
            _uploadStatus.postValue("Failed to download image: ${exception.message}")
        }
    }

}
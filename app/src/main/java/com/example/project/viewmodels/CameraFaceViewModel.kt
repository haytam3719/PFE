package com.example.project.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.TextureView
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraFaceViewModel @Inject constructor(@SuppressLint("StaticFieldLeak") private val context: Context) : ViewModel() {
    var imageCapture: ImageCapture? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    @SuppressLint("StaticFieldLeak")
    private lateinit var previewView: TextureView // Define previewView

    private val _navigateToViewPager = MutableLiveData<Boolean>()
    val navigateToViewPager: LiveData<Boolean>
        get() = _navigateToViewPager


    private val _faceUrl = MutableLiveData<String>()
    val faceUrl: LiveData<String> = _faceUrl

    private lateinit var numCin: String
    private val _data = MutableLiveData<String>()
    val dataFromFragment: LiveData<String> = _data

    fun setData(newData: String) {
        _data.value = newData
    }

    init {
        // Observe changes to the LiveData object
        dataFromFragment.observeForever { newData ->
            // Handle the new data as needed
            // This will be triggered whenever setDataFromFragment is called from the fragment
            numCin = newData
            Log.d("CameraFaceViewModel", "Data from fragment CameraFace: $newData")
            Log.d("CameraFaceViewModel", "numCin initialized: $numCin")
        }
    }

    // Setter method to initialize previewView
    fun setPreviewView(view: TextureView) {
        this.previewView = view
    }

    private val _imageSavedEvent = MutableLiveData<File>()
    val imageSavedEvent: LiveData<File>
        get() = _imageSavedEvent

    private val _cameraPermissionDeniedEvent = MutableLiveData<Unit>()
    val cameraPermissionDeniedEvent: LiveData<Unit>
        get() = _cameraPermissionDeniedEvent

    fun initImageCapture(imageCapture: ImageCapture) {
        this.imageCapture = imageCapture
    }

    fun takePhoto(identityCardNumber: String) {
        val imageCapture = imageCapture ?: return
        val photoFile = File(context.externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")
        val photoOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            photoOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Image capture success
                    val savedUri = Uri.fromFile(photoFile)
                    uploadImageToStorage(savedUri, identityCardNumber)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Image capture error
                    Log.e("Test", "Error taking photo: ${exception.message}", exception)
                }
            }
        )
    }

    private fun uploadImageToStorage(photoUri: Uri, identityCardNumber: String) {
        // Determine the folder structure based on the type of photo
        val folderName = "clients/$identityCardNumber/face"

        // Specify a valid storage location using getReference()
        val imageRef = storageRef.child("$folderName/${photoUri.lastPathSegment}")

        // Upload the file to the specified location
        imageRef.putFile(photoUri)
            .addOnSuccessListener { _ ->
                // Handle successful upload
                _imageSavedEvent.postValue(File(photoUri.path ?: ""))

                _faceUrl.postValue("$folderName/${photoUri.lastPathSegment}")


                _navigateToViewPager.postValue(true)

            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
                // Log the error or show an error message
                Log.e("Test", "Error uploading image to storage: $exception")
            }
    }


    fun onNavigationComplete() {
        _navigateToViewPager.value = false
    }


    private var previewSize: Size? = null

    fun getPreviewSize(): Size {
        if (previewSize == null) {
            previewSize = fetchPreviewSize()
        }
        return previewSize!!
    }

    private fun fetchPreviewSize(): Size {
        val cameraManager = context.getSystemService(CameraManager::class.java)
        val cameraId = cameraManager.cameraIdList[0]
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = map!!.getOutputSizes(SurfaceTexture::class.java)


        return sizes.maxByOrNull { it.width * it.height } ?: sizes[0]
    }

}

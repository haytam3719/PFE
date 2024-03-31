package com.example.project.viewmodels
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.TextureView
import android.view.View
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
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
class CameraCinViewModel @Inject constructor(@SuppressLint("StaticFieldLeak") private val context: Context) : ViewModel() {
    private lateinit var surfaceProvider: Preview.SurfaceProvider
    var imageCapture: ImageCapture? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    @SuppressLint("StaticFieldLeak")
    private lateinit var previewView: TextureView // Define previewView
    private var photoCount: Int = 0
    private val maxPhotoCount: Int = 3
    private val _navigateToScanFace = MutableLiveData<Boolean>()
    val navigateToScanFace: LiveData<Boolean>
        get() = _navigateToScanFace

    private val _imageFrontUrl = MutableLiveData<String>()
    val imageFrontUrl: LiveData<String>
        get() = _imageFrontUrl

    private val _imageBackUrl = MutableLiveData<String>()
    val imageBackUrl: LiveData<String>
        get() = _imageBackUrl


    private lateinit var numCin:String
    private val _data = MutableLiveData<String>()
    val dataFromFragment: LiveData<String> = _data

    fun setData(newData: String) {
        _data.value = newData
    }


    private val _filePathInfo = MutableLiveData<String>()
    val filePathInfo: LiveData<String>
        get() = _filePathInfo

    // Function to update the infoData LiveData
    fun updateInfoData(data: String) {
        _filePathInfo.value = data
    }


    init {
        // Observe changes to the LiveData object
        dataFromFragment.observeForever { newData ->
            // Handle the new data as needed
            // This will be triggered whenever setDataFromFragment is called from the fragment
            numCin=newData
            Log.d("CameraCinViewModel", "Data from fragment CameraCin: $newData")
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



    fun takePhoto(identityCardNumber: String, photoType: String) {
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
                    uploadImageToStorage(savedUri, identityCardNumber, photoType)
                    // Call the second takePhoto function here if necessary
                    if (photoType == "identity_card_front" && !isPhotoLimitReached()) {
                        takePhoto(identityCardNumber, "identity_card_back")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    // Image capture error
                    Log.e("Test", "Error taking photo: ${exception.message}", exception)
                }
            }
        )
    }



    private fun uploadImageToStorage(photoUri: Uri, identityCardNumber: String, photoType: String) {
        // Determine the folder structure based on the type of photo
        val folderName = when (photoType) {
            "identity_card_front" -> "clients/$identityCardNumber/cin_recto"
            "identity_card_back" -> "clients/$identityCardNumber/cin_verso"
            else -> "images" // Default to 'images' folder if type not recognized
        }

        // Specify a valid storage location using getReference()
        val imageRef = storageRef.child("$folderName/${photoUri.lastPathSegment}")

        // Upload the file to the specified location
        imageRef.putFile(photoUri)
            .addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                // Notify fragment or store the URL as needed
                _imageSavedEvent.postValue(File(photoUri.path ?: ""))
                if (photoType == "identity_card_front") {
                    _imageFrontUrl.postValue(downloadUrl)
                } else if (photoType == "identity_card_back") {
                    _imageBackUrl.postValue(downloadUrl)
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful upload
                // Log the error or show an error message
                Log.e("Test", "Error uploading image to storage: $exception")
            }
    }




    fun setSurfaceProvider(surfaceProvider: Preview.SurfaceProvider) {
        this.surfaceProvider = surfaceProvider
    }


    fun incrementPhotoCount() {
        photoCount++
    }

    // Method to check if the limit has been reached
    fun isPhotoLimitReached(): Boolean {
        return photoCount >= maxPhotoCount
    }

    // Reset the photo count when needed
    fun resetPhotoCount() {
        photoCount = 0
    }


    fun onButtonClick(view: View){
        if (!this.isPhotoLimitReached()) {
            this.takePhoto(numCin,"identity_card_front")
            incrementPhotoCount()
        }

        if (!this.isPhotoLimitReached()) {
            this.takePhoto(numCin,"identity_card_back")
            incrementPhotoCount()
        }
        // Check if the limit has been reached after taking the photo
        if (this.isPhotoLimitReached()) {
            _navigateToScanFace.value=true


        }
    }

    fun onNavigationComplete() {
        _navigateToScanFace.value = false
    }

    }


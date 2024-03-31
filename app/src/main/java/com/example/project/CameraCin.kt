package com.example.project

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.CameraCinBinding
import com.example.project.viewmodels.CameraCinViewModel
import com.example.project.viewmodels.CollectInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class CameraCin : Fragment() {
    private val cameraCinViewModel: CameraCinViewModel by viewModels()
    private lateinit var textureView: TextureView
    private val CAMERA_PERMISSION_REQUEST_CODE=100

    private lateinit var surfaceProvider: Preview.SurfaceProvider
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private var numCin: String=""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureView=view.findViewById(R.id.textureView)
        val executor = Executor { command -> command.run() }

        collectInfoViewModel.observeClient().observe(viewLifecycleOwner,  Observer { client ->
            // Handle client update here
            // You can update UI or perform any other actions based on the client data
            // For example, log the client data
            numCin=client.numCin
            Log.e("CIN", client.numCin)
            setDataToViewModel(numCin)

        })


        cameraCinViewModel.imageFrontUrl.observe(viewLifecycleOwner, Observer { imageUrl ->
            collectInfoViewModel.observeClient().value?.let { client ->
                client.identityCardFrontUrl = imageUrl
                // Update the client object in the repository or perform other actions if needed
                collectInfoViewModel.updateClient(client)
            }
        })

        cameraCinViewModel.imageBackUrl.observe(viewLifecycleOwner, Observer { imageUrl ->
            collectInfoViewModel.observeClient().value?.let { client ->
                client.identityCardBackUrl = imageUrl
                // Update the client object in the repository or perform other actions if needed
                collectInfoViewModel.updateClient(client)
            }
        })

        cameraCinViewModel.navigateToScanFace.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navigate when event is triggered
                findNavController().navigate(R.id.cameracin_to_scanface)

                // Reset the navigation event after navigation
                cameraCinViewModel.onNavigationComplete()
            }
        })


        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                surfaceProvider = Preview.SurfaceProvider { request ->
                    val surface = android.view.Surface(textureView.surfaceTexture)
                    request.provideSurface(surface, executor,{})
                }

                cameraCinViewModel.setSurfaceProvider(surfaceProvider)

            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                // Handle surface size change
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                // Clean up resources
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // Handle surface texture updates
            }
        }

        checkCameraPermission()

    }

    fun setDataToViewModel(data: String) {
        cameraCinViewModel.setData(data)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CameraCinBinding.inflate(inflater, container, false)
        binding.cameraVm = cameraCinViewModel

        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root


    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(surfaceProvider) }

                val imageCapture = ImageCapture.Builder()
                    .build()

                cameraCinViewModel.initImageCapture(imageCapture)
                Log.d("Debug", "Preview: $preview, ImageCapture: $imageCapture")
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Choose the back camera
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                // Handle any errors
                Log.e("CameraCin", "Error starting camera", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }




    private fun checkCameraPermission() {
        // Check if camera permission is not granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Camera permission is already granted, proceed with camera initialization
            initializeCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if the permission request is for camera permission
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Check if the permission was granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, proceed with camera initialization
                initializeCamera()
            } else {
                // Camera permission denied, handle accordingly (e.g., show a message or disable camera functionality)
            }
        }
    }

    private fun initializeCamera() {
        startCamera()
    }


}
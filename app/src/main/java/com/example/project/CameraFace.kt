package com.example.project

import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.CameraFaceBinding
import com.example.project.viewmodels.CameraFaceViewModel
import com.example.project.viewmodels.CollectInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class CameraFace : Fragment() {
    private val cameraFaceViewModel: CameraFaceViewModel by viewModels({requireActivity()})
    private val collectInfoViewModel: CollectInfoViewModel by viewModels({ requireActivity() })
    private var _binding: CameraFaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var textureView: TextureView
    private var numCin:String=""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureView = view.findViewById(R.id.textureViewF)
        val executor = Executor { command -> command.run() }


        collectInfoViewModel.observeClient().observe(viewLifecycleOwner, Observer { client ->
            // Handle client update here
            // You can update UI or perform any other actions based on the client data
            // For example, log the client data
            numCin = client.numCin
            Log.e("CIN", client.numCin)
            Log.d("CameraFaceFragment", "numCin: $numCin")
            setDataToViewModel(numCin)
            val email = "${client.numCin}@example.com"
            val password="haytam123"
            Log.e("The Client : ",client.toString())
            //collectInfoViewModel.signUpClient(email,password,client)
            cameraFaceViewModel.faceUrl.observe(viewLifecycleOwner){facePhotoUrl->
                client.facePhotoUrl = facePhotoUrl

            }
        })


        binding.captureButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            numCin.let { cameraFaceViewModel.takePhoto(it) }
        }


        if (binding.textureViewF.isAvailable) {
            startCamera()
        } else {
            binding.textureViewF.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                @RequiresApi(Build.VERSION_CODES.P)
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    startCamera()
                    //updateTransform(width, height)
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
//
                    //updateTransform(width, height)
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        }

        cameraFaceViewModel.navigateToViewPager.observe(viewLifecycleOwner, Observer { shouldNavigate->
            if(shouldNavigate){
                findNavController().navigate(R.id.cameraface_to_viewpager)
            }
        })

    }

    private fun setDataToViewModel(data: String) {
        cameraFaceViewModel.setData(data)
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(binding.textureViewF.display.rotation)
                    .build()

                preview.setSurfaceProvider { request ->
                    val surface = Surface(binding.textureViewF.surfaceTexture)
                    request.provideSurface(surface, ContextCompat.getMainExecutor(requireContext())) {}
                }

                val imageCapture = ImageCapture.Builder()
                    .setTargetRotation(binding.textureViewF.display.rotation)
                    .build() // No aspect ratio set for ImageCapture


                cameraFaceViewModel.initImageCapture(imageCapture)
                Log.d("Debug", "Preview: $preview, ImageCapture: $imageCapture")

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT) // Choose the front camera
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                // Handle any errors
                Log.e("CameraCin", "Error starting camera", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CameraFaceBinding.inflate(inflater, container, false)
        binding.cameraFaceViewModel=cameraFaceViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root


    }


}
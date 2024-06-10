package com.example.project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.project.databinding.FaceIdBinding
import com.example.project.viewmodels.FaceIDViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaceID : Fragment() {

    private var _binding: FaceIdBinding? = null
    private val binding get() = _binding!!

    private val faceIDViewModel:FaceIDViewModel by viewModels()

    private val REQUEST_CAMERA_PERMISSION = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FaceIdBinding.inflate(inflater, container, false)
        openVideoFeed()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {

            faceIDViewModel.uploadStatus.observe(viewLifecycleOwner, Observer { status ->
                println(status)
            })

            faceIDViewModel.finalDecision.observe(viewLifecycleOwner, Observer { decision ->
                println(decision)
            })

        }

        val referenceImagePath = "path_to_reference_image"
        faceIDViewModel.uploadReferenceImage(referenceImagePath)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        }
    }




    private fun openVideoFeed() {
        val videoFeedUrl = "http://127.0.0.1:5000/video_feed"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoFeedUrl))
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

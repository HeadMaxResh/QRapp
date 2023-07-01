package com.example.qrapp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.qrapp.databinding.FragmentCamBinding
import com.example.qrapp.domain.util.BarcodeScanner
import com.example.qrapp.domain.util.BarcodeScannerRepository
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CamFragment : Fragment(), BarcodeScannerRepository {

    private var _binding: FragmentCamBinding? = null
    private val binding: FragmentCamBinding
        get() = _binding ?: throw java.lang.RuntimeException("FragmentCamBinding = null")

    private val executorService: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val barcodeScanner by lazy {
        BarcodeScanner(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCamBinding.inflate(layoutInflater, container, false)
        setupCamera()
        return binding.root
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && isCameraPermissionGranted()) {
            startCamera()
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && isCameraPermissionGranted()) {
            startCamera()
        }
    }*/

    private fun setupCamera() {
        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            requestPermission()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder().build().apply {
                setAnalyzer(executorService, getImageAnalyzerListener())
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (throwable: Throwable) {
                Log.e(TAG, "Use case binding failed", throwable)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun getImageAnalyzerListener(): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer {
            imageProxy ->
            val image = imageProxy.image ?: return@Analyzer
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.scanImage(inputImage) {
                imageProxy.close()
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        barcodeScanner.closeScanner()
        executorService.shutdown()
    }

    override fun onScanSuccess(result: List<Barcode>) {
        result.forEachIndexed { _, barcode ->
            Toast.makeText(
                requireContext(),
                "Barcode value: ${barcode.rawValue}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onScanFailed() {
        Toast.makeText(requireContext(), "Scan fail", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val TAG = "CAM_FRAGMENT"
    }
}
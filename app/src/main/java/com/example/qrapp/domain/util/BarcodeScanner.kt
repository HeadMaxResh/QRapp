package com.example.qrapp.domain.util

import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanner(private val barcodeScannerRepository: BarcodeScannerRepository) {

    private val barcodeScanner: BarcodeScanner by lazy {
        constructBarcodeScanner()
    }

    private val executorService: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private fun constructBarcodeScanner(): BarcodeScanner {
        val barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setExecutor(executorService)
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_PDF417
            )
            .build()
        return BarcodeScanning.getClient(barcodeScannerOptions)
    }

    fun scanImage(inputImage: InputImage, onScanComplete: (() -> Unit)? = null) {
        barcodeScanner.process(inputImage)
            .addOnCompleteListener {
                onScanComplete?.invoke()
            }
            .addOnSuccessListener {
                barcodeScannerRepository.onScanSuccess(it)
            }
            .addOnFailureListener {
                Log.e("Scanner fail", "caused:", it)
                barcodeScannerRepository.onScanFailed()
            }
    }

    fun closeScanner() {
        barcodeScanner.close()
        executorService.shutdown()
    }
}
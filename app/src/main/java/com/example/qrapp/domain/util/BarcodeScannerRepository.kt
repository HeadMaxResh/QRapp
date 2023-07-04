package com.example.qrapp.domain.util

import com.google.mlkit.vision.barcode.common.Barcode

interface BarcodeScannerRepository {

    fun onScanSuccess(result: List<Barcode>)

    fun onScanFailed()
}
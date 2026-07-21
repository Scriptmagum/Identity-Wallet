package com.example.identitywallet.ui

import com.example.identitywallet.R

class SquareCaptureActivity : com.journeyapps.barcodescanner.CaptureActivity() {

    override fun initializeContent(): com.journeyapps.barcodescanner.DecoratedBarcodeView {
        setContentView(R.layout.activity_square_capture)
        return findViewById(R.id.zxing_barcode_scanner)
    }
}

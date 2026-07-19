package com.example.identitywallet.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QrDisplayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QR_JSON = "extra_qr_json"
        private const val VALIDITY_MS = 120_000L // 2 minutes — doit matcher la fenêtre vérifiée plus tard côté vérificateur
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.identitywallet.R.layout.activity_qr_display)

        val json = intent.getStringExtra(EXTRA_QR_JSON)
        if (json == null) {
            Toast.makeText(this, "Erreur : données QR manquantes", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val imageView = findViewById<ImageView>(com.example.identitywallet.R.id.image_qr)
        val countdownText = findViewById<TextView>(com.example.identitywallet.R.id.text_countdown)

        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(json, BarcodeFormat.QR_CODE, 800, 800)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur génération QR : ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        object : CountDownTimer(VALIDITY_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                countdownText.text = "Expire dans ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                countdownText.text = "QR expiré — représentez le document"
            }
        }.start()
    }
}
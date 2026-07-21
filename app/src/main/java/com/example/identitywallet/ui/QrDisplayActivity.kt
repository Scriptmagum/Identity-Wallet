package com.example.identitywallet.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.identitywallet.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QrDisplayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QR_JSON = "extra_qr_json"
        private const val VALIDITY_MS = 120_000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_display)

        @Suppress("DEPRECATION")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(
                android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars()
            )
            window.insetsController?.systemBarsBehavior =
                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val json = intent.getStringExtra(EXTRA_QR_JSON)
        if (json == null) {
            Toast.makeText(this, "Erreur : données QR manquantes", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val imageView = findViewById<ImageView>(R.id.image_qr)
        val countdownText = findViewById<TextView>(R.id.text_countdown)
        val doneButton = findViewById<Button>(R.id.button_done)

        doneButton.setOnClickListener { finish() }

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
                doneButton.visibility = android.view.View.VISIBLE
            }
        }.start()
    }
}

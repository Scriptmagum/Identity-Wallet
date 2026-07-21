package com.example.identitywallet.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.identitywallet.R
import com.example.identitywallet.crypto.QrVerifier
import com.google.android.material.appbar.MaterialToolbar
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanVerifyActivity : AppCompatActivity() {

    private lateinit var resultText: TextView

    private val scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (scanResult?.contents == null) {
            resultText.text = "Scan annulé"
            return@registerForActivityResult
        }
        lifecycleScope.launch {
            val verification = withContext(Dispatchers.IO) { QrVerifier.verify(scanResult.contents) }
            when (verification) {
                is QrVerifier.Result.Valid -> {
                    val data = verification.identityData
                    resultText.text = "✅ Identité vérifiée\n\n${data.prenom} ${data.nom}\nNé(e) le ${data.dateNaissance}\n${data.nationalite}"
                }
                is QrVerifier.Result.Invalid -> {
                    resultText.text = "❌ Vérification échouée\n\n${verification.reason}"
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchScan()
        } else {
            resultText.text = "Permission caméra refusée"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_verify)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        resultText = findViewById(R.id.text_result)

        findViewById<Button>(R.id.button_launch_scan).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchScan()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchScan() {
        scanLauncher.launch(
            IntentIntegrator(this)
                .setCaptureActivity(SquareCaptureActivity::class.java)
                .setOrientationLocked(false)
                .setPrompt("Cadrez le QR code à vérifier")
                .createScanIntent()
        )
    }
}

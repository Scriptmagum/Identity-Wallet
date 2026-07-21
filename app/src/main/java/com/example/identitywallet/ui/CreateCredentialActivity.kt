package com.example.identitywallet.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.identitywallet.R
import com.example.identitywallet.data.CredentialRepository
import com.example.identitywallet.model.CredentialType
import com.example.identitywallet.model.IdentityData
import com.google.android.material.appbar.MaterialToolbar

class CreateCredentialActivity : AppCompatActivity() {

    private lateinit var repository: CredentialRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_credential)

        repository = CredentialRepository(this)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val spinnerType = findViewById<Spinner>(R.id.spinner_type)
        spinnerType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CredentialType.entries.map { it.displayName }
        )

        findViewById<Button>(R.id.button_create).setOnClickListener {
            val nom = findViewById<EditText>(R.id.edit_nom).text.toString()
            val prenom = findViewById<EditText>(R.id.edit_prenom).text.toString()
            val dateNaissance = findViewById<EditText>(R.id.edit_date_naissance).text.toString()
            val nationalite = findViewById<EditText>(R.id.edit_nationalite).text.toString()
            val numeroDocument = findViewById<EditText>(R.id.edit_numero_document).text.toString()

            if (nom.isBlank() || prenom.isBlank()) {
                Toast.makeText(this, "Nom et prénom sont obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
            progressBar.visibility = View.VISIBLE

            val type = CredentialType.entries[spinnerType.selectedItemPosition]
            val identityData = IdentityData(nom, prenom, dateNaissance, nationalite, numeroDocument)

            BiometricAuthHelper.authenticate(
                activity = this,
                onSuccess = {
                    try {
                        repository.createCredential(type, identityData)
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Document créé et certifié", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                onError = { message ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Authentification échouée : $message", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

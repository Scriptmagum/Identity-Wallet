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
import com.example.identitywallet.data.CredentialRepository
import com.example.identitywallet.model.CredentialType
import com.example.identitywallet.model.IdentityData

class CreateCredentialActivity : AppCompatActivity() {

    private lateinit var repository: CredentialRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.identitywallet.R.layout.activity_create_credential)

        repository = CredentialRepository(this)

        val spinnerType = findViewById<Spinner>(com.example.identitywallet.R.id.spinner_type)
        spinnerType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CredentialType.entries.map { it.displayName }
        )

        findViewById<Button>(com.example.identitywallet.R.id.button_create).setOnClickListener {
            val nom = findViewById<EditText>(com.example.identitywallet.R.id.edit_nom).text.toString()
            val prenom = findViewById<EditText>(com.example.identitywallet.R.id.edit_prenom).text.toString()
            val dateNaissance = findViewById<EditText>(com.example.identitywallet.R.id.edit_date_naissance).text.toString()
            val nationalite = findViewById<EditText>(com.example.identitywallet.R.id.edit_nationalite).text.toString()
            val numeroDocument = findViewById<EditText>(com.example.identitywallet.R.id.edit_numero_document).text.toString()

            if (nom.isBlank() || prenom.isBlank()) {
                Toast.makeText(this, "Nom et prénom sont obligatoires", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val progressBar = findViewById<ProgressBar>(com.example.identitywallet.R.id.progress_bar)
            progressBar.visibility = View.VISIBLE

            val spinnerType = findViewById<Spinner>(com.example.identitywallet.R.id.spinner_type)
            val type = CredentialType.entries[spinnerType.selectedItemPosition]
            val identityData = IdentityData(nom, prenom, dateNaissance, nationalite, numeroDocument)

            BiometricAuthHelper.authenticate(
                activity = this,
                onSuccess = {
                    try {
                        repository.createCredential(type, identityData)
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Document créé et certifié ✅", Toast.LENGTH_SHORT).show()
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

    private fun createCredential(spinnerType: Spinner) {
        val nom = findViewById<EditText>(com.example.identitywallet.R.id.edit_nom).text.toString()
        val prenom = findViewById<EditText>(com.example.identitywallet.R.id.edit_prenom).text.toString()
        val dateNaissance = findViewById<EditText>(com.example.identitywallet.R.id.edit_date_naissance).text.toString()
        val nationalite = findViewById<EditText>(com.example.identitywallet.R.id.edit_nationalite).text.toString()
        val numeroDocument = findViewById<EditText>(com.example.identitywallet.R.id.edit_numero_document).text.toString()

        if (nom.isBlank() || prenom.isBlank()) {
            Toast.makeText(this, "Nom et prénom sont obligatoires", Toast.LENGTH_SHORT).show()
            return
        }

        val progressBar = findViewById<ProgressBar>(com.example.identitywallet.R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        val type = CredentialType.entries[spinnerType.selectedItemPosition]
        val identityData = IdentityData(nom, prenom, dateNaissance, nationalite, numeroDocument)

        // Génération de clés + chiffrement + certification — rapide, mais fait sur le thread
        // principal pour l'instant par simplicité (à améliorer avec coroutines si le temps permet)
        repository.createCredential(type, identityData)

        progressBar.visibility = View.GONE
        Toast.makeText(this, "Document créé et certifié ✅", Toast.LENGTH_SHORT).show()
        finish() // retourne à MainActivity, qui rechargera la liste via onResume()
    }
}
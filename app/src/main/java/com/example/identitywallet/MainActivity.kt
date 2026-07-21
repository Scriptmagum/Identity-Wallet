package com.example.identitywallet

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.identitywallet.data.CredentialEntity
import com.example.identitywallet.data.CredentialRepository
import com.example.identitywallet.ui.BiometricAuthHelper
import com.example.identitywallet.ui.CredentialCardAdapter
import com.example.identitywallet.ui.QrDisplayActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var repository: CredentialRepository
    private var pendingDelete: CredentialEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = CredentialRepository(this)

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            startActivity(android.content.Intent(this, com.example.identitywallet.ui.CreateCredentialActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.button_scan).setOnClickListener {
            startActivity(android.content.Intent(this, com.example.identitywallet.ui.ScanVerifyActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val credentials = repository.getAll()

        val emptyState = findViewById<android.view.View>(R.id.empty_state)
        val recyclerView = findViewById<RecyclerView>(R.id.list_credentials)

        emptyState.visibility = if (credentials.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        recyclerView.visibility = if (credentials.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = CredentialCardAdapter(
            items = credentials,
            onItemClick = { entity ->
                BiometricAuthHelper.authenticate(
                    activity = this,
                    onSuccess = {
                        try {
                            val payload = repository.buildQrPayload(entity)
                            val intent = android.content.Intent(this, QrDisplayActivity::class.java)
                            intent.putExtra(QrDisplayActivity.EXTRA_QR_JSON, payload.toJson())
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { message ->
                        Toast.makeText(this, "Authentification échouée : $message", Toast.LENGTH_LONG).show()
                    }
                )
            },
            onItemLongClick = { entity ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Supprimer")
                    .setMessage("Voulez-vous supprimer « ${entity.type.displayName} » ?")
                    .setPositiveButton("Oui") { _, _ ->
                        repository.deleteById(entity.id)
                        refreshList()
                    }
                    .setNegativeButton("Non", null)
                    .show()
            }
        )

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos < 0 || pos >= credentials.size) return
                val entity = credentials[pos]
                pendingDelete = entity
                repository.removeFromStorage(entity.id)
                Snackbar.make(recyclerView, "Document supprimé", Snackbar.LENGTH_LONG)
                    .setAction("Annuler") {
                        pendingDelete?.let { repository.restoreEntity(it) }
                        pendingDelete = null
                        refreshList()
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                pendingDelete?.let { repository.deleteKeys(it.id) }
                                pendingDelete = null
                            }
                        }
                    })
                    .show()
                refreshList()
            }
        }).attachToRecyclerView(recyclerView)
    }
}

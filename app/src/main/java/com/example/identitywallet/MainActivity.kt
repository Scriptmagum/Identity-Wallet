package com.example.identitywallet

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.identitywallet.data.CredentialRepository
import com.example.identitywallet.ui.CredentialListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var repository: CredentialRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = CredentialRepository(this)

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            startActivity(android.content.Intent(this, com.example.identitywallet.ui.CreateCredentialActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList() // recharge à chaque retour sur cet écran (après création/suppression)
    }

    private fun refreshList() {
        val credentials = repository.getAll()

        val emptyText = findViewById<android.widget.TextView>(R.id.text_empty)
        val listView = findViewById<ListView>(R.id.list_credentials)

        emptyText.visibility = if (credentials.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        listView.visibility = if (credentials.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE

        listView.adapter = CredentialListAdapter(this, credentials)
    }
}
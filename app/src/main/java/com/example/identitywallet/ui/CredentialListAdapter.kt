package com.example.identitywallet.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.identitywallet.crypto.MockCA
import com.example.identitywallet.data.CredentialEntity

/**
 * Adapter simple pour afficher la liste des credentials dans la ListView.
 * Utilise le layout Android natif à 2 lignes (pas besoin de créer un XML custom).
 */
class CredentialListAdapter(
    context: Context,
    private val items: List<CredentialEntity>
) : ArrayAdapter<CredentialEntity>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)

        val entity = items[position]
        val isCertified = MockCA.verifyCertificate(entity.certificate)

        val title = view.findViewById<TextView>(android.R.id.text1)
        val subtitle = view.findViewById<TextView>(android.R.id.text2)

        title.text = entity.type.displayName
        subtitle.text = if (isCertified) "✅ Certifié" else "⚠️ Certificat invalide/expiré"

        return view
    }
}
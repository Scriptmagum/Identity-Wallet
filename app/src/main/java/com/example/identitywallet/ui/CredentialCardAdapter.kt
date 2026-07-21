package com.example.identitywallet.ui

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.identitywallet.R
import com.example.identitywallet.crypto.MockCA
import com.example.identitywallet.data.CredentialEntity
import com.example.identitywallet.model.CredentialType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CredentialCardAdapter(
    private val items: List<CredentialEntity>,
    private val onItemClick: (CredentialEntity) -> Unit,
    private val onItemLongClick: (CredentialEntity) -> Unit
) : RecyclerView.Adapter<CredentialCardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_credential_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entity = items[position]
        holder.bind(entity, onItemClick, onItemLongClick)
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: View = itemView.findViewById(R.id.icon_container)
        private val iconType: ImageView = itemView.findViewById(R.id.icon_type)
        private val textTitle: TextView = itemView.findViewById(R.id.text_title)
        private val textStatus: TextView = itemView.findViewById(R.id.text_status)
        private val textDetail: TextView = itemView.findViewById(R.id.text_detail)

        fun bind(
            entity: CredentialEntity,
            onItemClick: (CredentialEntity) -> Unit,
            onItemLongClick: (CredentialEntity) -> Unit
        ) {
            val ctx = itemView.context
            textTitle.text = entity.type.displayName

            val isCertified = MockCA.verifyCertificate(entity.certificate)

            when (entity.type) {
                CredentialType.CARTE_IDENTITE -> {
                    iconType.setImageResource(R.drawable.ic_card_id)
                    setIconTint(ctx, R.color.card_id)
                }
                CredentialType.PERMIS_CONDUIRE -> {
                    iconType.setImageResource(R.drawable.ic_card_driving)
                    setIconTint(ctx, R.color.card_driving)
                }
                CredentialType.CARTE_ETUDIANT -> {
                    iconType.setImageResource(R.drawable.ic_card_student)
                    setIconTint(ctx, R.color.card_student)
                }
            }

            if (isCertified) {
                textStatus.text = "Certifié"
                textStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_status_certified, 0, 0, 0)
                textStatus.setTextColor(ContextCompat.getColor(ctx, R.color.card_certified))
            } else {
                textStatus.text = "Certificat invalide"
                textStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_status_invalid, 0, 0, 0)
                textStatus.setTextColor(ContextCompat.getColor(ctx, R.color.card_invalid))
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            val dateStr = dateFormat.format(Date(entity.createdAt))
            textDetail.text = "N° ${entity.id.take(8)} · $dateStr"

            itemView.setOnClickListener { onItemClick(entity) }
            itemView.setOnLongClickListener {
                onItemLongClick(entity)
                true
            }
        }

        private fun setIconTint(ctx: android.content.Context, colorRes: Int) {
            val bg = iconContainer.background as GradientDrawable
            bg.setColor(ContextCompat.getColor(ctx, colorRes))
            bg.invalidateSelf()
        }
    }
}

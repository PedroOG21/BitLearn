// AmigosAdapter.kt
package com.example.proyectoandroid.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.AmigosModel
import com.example.proyectoandroid.databinding.AmigosLayoutBinding
import com.squareup.picasso.Picasso

class AmigosAdapter(
    private val items: List<AmigosModel>,
    private val onDelete: (AmigosModel) -> Unit,
    private val onEdit: (AmigosModel) -> Unit
) : RecyclerView.Adapter<AmigosAdapter.AmigosViewHolder>() {

    inner class AmigosViewHolder(val binding: AmigosLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(amigo: AmigosModel) {
            binding.tvNombre.text = amigo.nombre
            binding.tvEmail.text = amigo.email

            // Protección contra URL vacía
            val url = amigo.imagen
            if (url.isNullOrBlank()) {
                binding.imageView.setImageResource(R.drawable.baseline_person_24)
            } else {
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(binding.imageView)
            }

            binding.btnBorrar.setOnClickListener { onDelete(amigo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmigosViewHolder {
        val binding = AmigosLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AmigosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AmigosViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

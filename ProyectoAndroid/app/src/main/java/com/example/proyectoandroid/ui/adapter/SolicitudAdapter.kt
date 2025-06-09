package com.example.proyectoandroid.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.SolicitudModel
import com.example.proyectoandroid.databinding.ItemSolicitudBinding
import com.squareup.picasso.Picasso

class SolicitudesAdapter(
    private val items: List<SolicitudModel>,
    private val onAccept: (SolicitudModel) -> Unit,
    private val onReject: (SolicitudModel) -> Unit
) : RecyclerView.Adapter<SolicitudesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSolicitudBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sol: SolicitudModel) {
            binding.tvNombre.text = sol.fromNombre

            val url = sol.fromImagen
            if (url.isNullOrBlank()) {
                binding.ivImagen.setImageResource(R.drawable.baseline_euro_24)
            } else {
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.baseline_euro_24)
                    .error(R.drawable.baseline_euro_24)
                    .into(binding.ivImagen)
            }

            binding.btnAccept.setOnClickListener { onAccept(sol) }
            binding.btnReject.setOnClickListener { onReject(sol) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemSolicitudBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

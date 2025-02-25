package com.example.proyectoandroid.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.AmigosModel
import com.example.proyectoandroid.databinding.AmigosLayoutBinding
import com.squareup.picasso.Picasso

class AmigosAdapter(
    var lista: MutableList<AmigosModel>,
    private val borrarAmigo: (Int) -> Unit,
    private val updateAmigo: (AmigosModel) -> Unit
) : RecyclerView.Adapter<AmigosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmigosViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.amigos_layout, parent, false)
        return AmigosViewHolder(v)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: AmigosViewHolder, position: Int) {
        holder.render(lista[position], borrarAmigo, updateAmigo)
    }

}

class AmigosViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val binding = AmigosLayoutBinding.bind(v)
    fun render(
        c: AmigosModel,
        borrarAmigo: (Int) -> Unit,
        updateAmigo: (AmigosModel) -> Unit
    ) {
        binding.tvNombre.text = c.nombre
        binding.tvEmail.text = c.email

        Picasso.get().load(c.imagen).into(binding.imageView)

        binding.btnBorrar.setOnClickListener {
            borrarAmigo(adapterPosition)
        }
        binding.btnUpdate.setOnClickListener {
            updateAmigo(c)
        }
    }
}

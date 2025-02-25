package com.example.proyectoandroid.ui.adapter

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ChatLayoutBinding
import com.example.proyectoandroid.domain.models.ChatModel
import java.text.SimpleDateFormat
import java.util.Date


class ChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val binding = ChatLayoutBinding.bind(v)
    fun render(
        item: ChatModel,
        emailUsuarioLogueado: String,
        eliminarMensaje: (ChatModel) -> Unit
    ) {
        val params = binding.cardViewChat.layoutParams as FrameLayout.LayoutParams

        if (emailUsuarioLogueado == item.email) {
            //podria ser cualquien elemento para sacar contexto, no hace falta que fuese tvFecha
            binding.clChat.setBackgroundColor(binding.tvFecha.context.getColor(R.color.color_logueado))
            params.gravity = Gravity.END
            //ponemos el boton visible para el usuario que estee logueado
            binding.btEliminar.visibility = View.VISIBLE
        } else {
            //podria ser cualquien elemento para sacar contexto, no hace falta que fuese tvFecha
            binding.clChat.setBackgroundColor(binding.tvFecha.context.getColor(R.color.color_normal))
            params.gravity = Gravity.START
            //boton invisible
            binding.btEliminar.visibility = View.GONE

        }
        binding.tvEmail.text = item.email
        binding.tvMensaje.text = item.mensaje
        binding.tvFecha.text = fechaFormateada(item.fecha)

        binding.btEliminar.setOnClickListener {
            eliminarMensaje(item)
        }
    }

    private fun fechaFormateada(fecha: Long): String {
        val date = Date(fecha)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return format.format(date)
    }

}

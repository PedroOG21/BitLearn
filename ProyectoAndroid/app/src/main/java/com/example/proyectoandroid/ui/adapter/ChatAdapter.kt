package com.example.proyectoandroid.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.domain.models.ChatModel


class ChatAdapter(
    var lista: MutableList<ChatModel>,
    private val emailUsuarioLogeado: String,
    private val eliminarMensaje: (ChatModel) -> Unit
) : RecyclerView.Adapter<ChatViewHolder>() {

    fun actualizarAdapter(listaNueva: MutableList<ChatModel>) {
        lista = listaNueva
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_layout, parent, false)
        return ChatViewHolder(v)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.render(lista[position], emailUsuarioLogeado, eliminarMensaje)
    }
}
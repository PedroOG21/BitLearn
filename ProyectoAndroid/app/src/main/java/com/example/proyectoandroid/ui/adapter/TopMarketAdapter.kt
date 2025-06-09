package com.example.proyectoandroid.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apicrypto.domain.models.CryptoCurrency
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.CryptoLayoutBinding
import com.example.proyectoandroid.utils.Constantes.GRAFICO
import com.example.proyectoandroid.utils.Constantes.IMG_URL
import com.squareup.picasso.Picasso


class TopMarketAdapter(
    var lista: MutableList<CryptoCurrency>, private val onItemClick: (CryptoCurrency) -> Unit
) : RecyclerView.Adapter<TopMarketViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopMarketViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.crypto_layout, parent, false)
        return TopMarketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopMarketViewHolder, position: Int) {
        val crypto = lista[position]
        holder.render(crypto, onItemClick)
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(newList: MutableList<CryptoCurrency>) {
        lista = newList
        notifyDataSetChanged()
    }

}

class TopMarketViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val binding = CryptoLayoutBinding.bind(v)

    fun render(crypto: CryptoCurrency, onItemClick: (CryptoCurrency) -> Unit) {
        binding.tvNombre.text = crypto.name

        binding.tvCorto.text = crypto.symbol

        Picasso.get().load("$IMG_URL${crypto.id}.png").into(binding.ivImagen)
        Picasso.get().load("$GRAFICO${crypto.id}.png").into(binding.ivGrafica1)


        val precio = crypto.quotes[0].price
        binding.tvPrecio.text = if (precio >= 1) String.format("$%.2f", precio)
        else String.format("$%.4f", precio)

        if (crypto.quotes[0].percentChange1h > 0) {
            binding.tvPorcentaje.setTextColor(Color.GREEN)
            binding.tvPorcentaje.text =
                "+${String.format("%.02f", crypto.quotes[0].percentChange1h)} %"
        } else {
            binding.tvPorcentaje.setTextColor(Color.RED)
            binding.tvPorcentaje.text =
                "${String.format("%.02f", crypto.quotes[0].percentChange1h)} %"
        }

        itemView.setOnClickListener { onItemClick(crypto) }
    }
}
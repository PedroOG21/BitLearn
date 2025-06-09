package com.example.proyectoandroid.ui.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ItemMiscriptosBinding
import com.example.proyectoandroid.domain.models.CryptoInventoryItem
import com.example.proyectoandroid.utils.Constantes.IMG_URL
import com.squareup.picasso.Picasso

class MisCriptosAdapter(
    private val items: List<CryptoInventoryItem>,
    private val onItemClick: (CryptoInventoryItem) -> Unit
) : RecyclerView.Adapter<MisCriptosAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMiscriptosBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CryptoInventoryItem) {
            // Imagen
            Picasso.get()
                .load("$IMG_URL${item.id}.png")
                .placeholder(R.drawable.baseline_euro_24)
                .into(binding.ivImagen2)

            binding.tvNombre2.text = item.name
            binding.tvNumeroMonedas.text = "%.4f".format(item.amount)
            binding.tvNombreCorto2.text = item.symbol

            binding.tvPrecioActual.text = if (item.price >= 1)
                String.format("$%.2f", item.price)
            else
                String.format("$%.6f", item.price)

            val total = item.totalValue
            binding.tvDineroMonedas.text = if (total >= 1)
                String.format("$%.2f", total)
            else
                String.format("$%.6f", total)

            binding.tvProcentaje2.apply {
                text =
                    "${if (item.percentChange > 0) "+" else ""}${"%.2f".format(item.percentChange)} %"
                setTextColor(if (item.percentChange > 0) Color.GREEN else Color.RED)
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemMiscriptosBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
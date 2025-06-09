package com.example.proyectoandroid.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ItemRankingBinding
import com.example.proyectoandroid.domain.models.RankingModel

class RankingAdapter(
    private val items: List<RankingModel>,
    private val user_id: String
) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRankingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RankingModel, position: Int) {

            binding.tvPosition.text = "${position + 4}."

            binding.tvName.text = item.nombre
            binding.tvCoins.text = item.monedas.toString()

            if (item.userId == user_id) {

                val color = ContextCompat.getColor(binding.root.context, R.color.colorAccent)
                binding.tvName.setTextColor(color)
                binding.tvCoins.setTextColor(color)
                binding.tvPosition.setTextColor(color)

                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.lightAccentBg)
                )
            } else {

                val textoPorDefecto =
                    ContextCompat.getColor(binding.root.context, R.color.textPrimary)
                val bgColor =
                    ContextCompat.getColor(binding.root.context, R.color.cardBackground)
                binding.tvName.setTextColor(textoPorDefecto)
                binding.tvCoins.setTextColor(textoPorDefecto)
                binding.tvPosition.setTextColor(textoPorDefecto)
                binding.root.setCardBackgroundColor(bgColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRankingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        holder.bind(items[pos], pos)
    }

    override fun getItemCount(): Int = items.size
}

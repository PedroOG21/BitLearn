package com.example.proyectoandroid.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apicrypto.domain.models.CryptoCurrency
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityDetalleCryptoBinding
import com.example.proyectoandroid.utils.Constantes.IMG_URL
import com.squareup.picasso.Picasso

class DetalleCryptoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleCryptoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetalleCryptoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mostrarDetallesCrypto()
        setListeners()
    }

    private fun setListeners() {
        binding.btExit.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDetallesCrypto() {
        // Recibe el objeto CryptoCurrency enviado a través del Intent
        val crypto = intent.getSerializableExtra("CRYPTO") as? CryptoCurrency
        crypto?.let {
            binding.tvNombreL.text = it.name
            binding.tvNombreL2.text = it.name

            binding.tvFechaEmision.text = it.dateAdded.substring(0, 10)

            binding.tvPorcentage.text = it.quotes[0].percentChange1h.toString()

            //CAMBIAR PARA QUE SALGAL EN MILLONES (M)
            binding.tvSuministroTotal.text = it.totalSupply.toString()
            binding.tvSuministracionCirculacion.text = it.circulatingSupply.toString()

            binding.tvCort.text = it.symbol


            val precio = crypto.quotes[0].price

            binding.tvPrecioo.text = if (precio >= 1)
                String.format("$%.2f", precio)
            else
                String.format("$%.7f", precio)

            if (crypto.quotes[0].percentChange1h > 0) {
                binding.tvPorcentage.setTextColor(Color.GREEN)
                binding.tvPorcentage.text =
                    "+${String.format("%.02f", crypto.quotes[0].percentChange1h)} %"
            } else {
                binding.tvPorcentage.setTextColor(Color.RED)
                binding.tvPorcentage.text =
                    "${String.format("%.02f", crypto.quotes[0].percentChange1h)} %"
            }

            Picasso.get().load("$IMG_URL${it.id}.png").into(binding.tvImage)
        }
    }
}
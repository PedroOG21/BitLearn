package com.example.proyectoandroid.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apicrypto.domain.models.CryptoCurrency
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityDetalleCryptoBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.Constantes.IMG_URL
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.squareup.picasso.Picasso

class DetalleCryptoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleCryptoBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var dbUser: DatabaseReference
    private var precioActual = 0.0
    private lateinit var cryptoId: String

    override fun attachBaseContext(newBase: Context) {
        val prefs = Preferences(newBase)
        val idioma = prefs.getIdioma()
        val context = LocaleHelper.wrap(newBase, idioma)
        super.attachBaseContext(context)
    }


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

        // Teclado
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        val crypto = intent.getSerializableExtra("CRYPTO") as? CryptoCurrency
            ?: throw IllegalStateException("No se recibió CryptoCurrency en el Intent")

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        dbUser = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(userId)

        precioActual = crypto.quotes[0].price
        cryptoId = crypto.id.toString()
        binding.btnBuy.setOnClickListener { comprarCrypto() }
        binding.btnSell.setOnClickListener { venderCripto() }

        mostrarDetallesCrypto()
        setListeners()
        cargarGráfica(
            binding.bt1h,
            "1H",
            crypto,
            binding.bt1s,
            binding.bt1d,
            binding.bt1a,
            binding.bt1h
        )
        botonesTiempo(crypto)
    }

    private fun botonesTiempo(item: CryptoCurrency) {
        val semana = binding.bt1s
        val dia = binding.bt1d
        val año = binding.bt1a
        val hora = binding.bt1h

        val boton = View.OnClickListener { it: View ->
            when (it.id) {
                semana.id -> cargarGráfica(it, "W", item, semana, dia, año, hora)
                dia.id -> cargarGráfica(it, "D", item, semana, dia, año, hora)
                año.id -> cargarGráfica(it, "12M", item, semana, dia, año, hora)
                hora.id -> cargarGráfica(it, "1H", item, semana, dia, año, hora)
            }
        }
        semana.setOnClickListener(boton)
        dia.setOnClickListener(boton)
        año.setOnClickListener(boton)
        hora.setOnClickListener(boton)
    }

    private fun cargarGráfica(
        clickedView: View,
        intervalo: String,
        item: CryptoCurrency,
        semana: Button,
        mes: Button,
        año: Button,
        hora: Button
    ) {

        desactivarBoton(semana, mes, año, hora, clickedView)
        clickedView.setBackgroundResource(R.drawable.bg_color)

        binding.wvGrafica.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.wvGrafica.settings.javaScriptEnabled = true


        val baseUrl = "https://s.tradingview.com/widgetembed/?"
        val simboloParametros = "symbol=BINANCE%3A${item.symbol}USD"     // ":" debe ir como %3A
        val parametros = listOf(
            simboloParametros,
            "interval=$intervalo",
            "hidesidetoolbar=1",
            "hidetoptoolbar=1",
            "symboledit=1",
            "saveimage=1",
            "toolbarbg=F1F3F6",
            "studies=[]",
            "hideideas=1",
            "theme=Dark",
            "style=1",
            "timezone=Etc%2FUTC",
            "locale=en",
            "utm_source=coinmarketcap.com",
            "utm_medium=widget",
            "utm_campaign=chart",
            "utm_term=${item.id}USD"
        )
        val url = baseUrl + parametros.joinToString("&")

        binding.wvGrafica.loadUrl(url)
    }

    private fun desactivarBoton(semana: Button, dia: Button, año: Button, hora: Button, it: View) {
        dia.background = null
        año.background = null
        hora.background = null
        semana.background = null
    }


    private fun setListeners() {
        binding.btExit.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDetallesCrypto() {
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

    private fun comprarCrypto() {
        val cantidad = binding.etAmount.text.toString().toDoubleOrNull()
        if (cantidad == null || cantidad <= 0) {
            binding.etAmount.error = getString(R.string.error_cantidad_invalida)
            return
        }
        val precio = cantidad * precioActual

        // Leer saldo
        dbUser.child("resumen").child("monedero")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val wallet = (snap.getValue(Long::class.java) ?: 0L).toDouble()
                    if (wallet < precio) {
                        Toast.makeText(this@DetalleCryptoActivity,
                            getString(R.string.error_fondos_insuficientes),
                            Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Posicion Actual
                    dbUser.child("criptos").child(cryptoId)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(posSnap: DataSnapshot) {
                                val cartera = posSnap.getValue(Double::class.java) ?: 0.0
                                val actualizar = mapOf(
                                    "resumen/monedero"  to (wallet - precio).toLong(),
                                    "criptos/$cryptoId" to (cartera + cantidad)
                                )
                                dbUser.updateChildren(actualizar)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@DetalleCryptoActivity,
                                            getString(R.string.toast_compra_ok, cantidad, binding.tvCort.text),
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@DetalleCryptoActivity,
                                            R.string.toast_compra_error, Toast.LENGTH_SHORT).show()
                                    }
                            }
                            override fun onCancelled(e: DatabaseError) {}
                        })
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun venderCripto() {
        val cantidad = binding.etAmount.text.toString().toDoubleOrNull()
        if (cantidad == null || cantidad <= 0) {
            binding.etAmount.error = getString(R.string.error_cantidad_invalida)
            return
        }

        dbUser.child("criptos").child(cryptoId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(posSnap: DataSnapshot) {
                    val cantidadActual = posSnap.getValue(Double::class.java) ?: 0.0
                    if (cantidadActual < cantidad) {
                        Toast.makeText(this@DetalleCryptoActivity,
                            getString(R.string.error_monedas_insuficientes),
                            Toast.LENGTH_SHORT).show()
                        return
                    }
                    val proceeds = cantidad * precioActual

                    dbUser.child("resumen").child("monedero")
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snap: DataSnapshot) {
                                val cartera = (snap.getValue(Long::class.java) ?: 0L).toDouble()
                                val actualizar = mapOf(
                                    "resumen/monedero"  to (cartera + proceeds).toLong(),
                                    "criptos/$cryptoId" to (cantidadActual - cantidad)
                                )
                                dbUser.updateChildren(actualizar)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@DetalleCryptoActivity,
                                            getString(R.string.toast_venta_ok, cantidad, binding.tvCort.text),
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@DetalleCryptoActivity,
                                            R.string.toast_venta_error, Toast.LENGTH_SHORT).show()
                                    }
                            }
                            override fun onCancelled(e: DatabaseError) {}
                        })
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }
}

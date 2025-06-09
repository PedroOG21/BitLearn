package com.example.proyectoandroid.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.net.ApiInterfaz
import com.example.proyectoandroid.data.net.CryptoObject
import com.example.proyectoandroid.databinding.ActivityMisCriptosBinding
import com.example.proyectoandroid.domain.models.CryptoInventoryItem
import com.example.proyectoandroid.ui.adapter.MisCriptosAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MisCriptosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisCriptosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUser: DatabaseReference

    private val items = mutableListOf<CryptoInventoryItem>()
    private lateinit var adapter: MisCriptosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMisCriptosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        binding.imageView7.setOnClickListener { finish() }

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return
        dbUser = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .child("criptos")

        adapter = MisCriptosAdapter(items) { item -> abrirDetalle(item) }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        cargarInventario()
    }

    private fun cargarInventario() {
        dbUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                items.clear()
                snapshot.children.forEach { ch ->
                    val cantidad = ch.getValue(Double::class.java) ?: return@forEach
                    if (cantidad <= 0.0) return@forEach
                    val id = ch.key ?: return@forEach
                    val item = CryptoInventoryItem(id, cantidad)
                    items.add(item)
                    detalleCryptos(item)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { /* opcional */
            }
        })
    }

    private fun detalleCryptos(item: CryptoInventoryItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = CryptoObject.getInstance()
                .create(ApiInterfaz::class.java)
                .getMarketData()
            if (response.isSuccessful) {
                val list = response.body()!!.data.cryptoCurrencyList
                val crypto = list.firstOrNull { it.id.toString() == item.id }
                crypto?.let {
                    withContext(Dispatchers.Main) {
                        item.apply {
                            name = it.name
                            symbol = it.symbol
                            price = it.quotes[0].price
                            percentChange = it.quotes[0].percentChange1h
                            totalValue = amount * price
                        }
                        val pos = items.indexOf(item)
                        if (pos != -1) adapter.notifyItemChanged(pos)
                    }
                }
            }
        }
    }

    private fun abrirDetalle(item: CryptoInventoryItem) {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = CryptoObject.getInstance()
                .create(ApiInterfaz::class.java)
                .getMarketData()
            if (response.isSuccessful) {
                val list = response.body()!!.data.cryptoCurrencyList
                val crypto = list.firstOrNull { it.id.toString() == item.id } ?: return@launch
                withContext(Dispatchers.Main) {
                    startActivity(
                        Intent(
                            this@MisCriptosActivity,
                            DetalleCryptoActivity::class.java
                        ).apply {
                            putExtra("CRYPTO", crypto)
                        })
                }
            }
        }
    }
}
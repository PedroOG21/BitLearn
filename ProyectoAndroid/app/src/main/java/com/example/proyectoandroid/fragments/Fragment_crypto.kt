package com.example.proyectoandroid.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apicrypto.domain.models.CryptoCurrency
import com.example.proyectoandroid.data.net.ApiInterfaz
import com.example.proyectoandroid.data.net.CryptoObject
import com.example.proyectoandroid.databinding.FragmentCryptoBinding
import com.example.proyectoandroid.ui.AmigosActivity
import com.example.proyectoandroid.ui.DetalleCryptoActivity
import com.example.proyectoandroid.ui.MisCriptosActivity
import com.example.proyectoandroid.ui.adapter.TopMarketAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Fragment_crypto : Fragment() {

    private lateinit var binding: FragmentCryptoBinding
    private var listaOriginal: List<CryptoCurrency> = listOf()
    private lateinit var adapter: TopMarketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCryptoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        obtenerListaMonedas()
        configurarBuscador()

    }

    private fun setListeners() {

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        // Asignar un adapter vacio inicialmente
        adapter = TopMarketAdapter(mutableListOf()) { crypto ->
            verDetalle(crypto)
        }

        binding.recycler.adapter = adapter

        binding.btAprende.setOnClickListener {
            val intent = Intent(requireContext(), MisCriptosActivity::class.java)
            startActivity(intent)
        }
        binding.btAmigos.setOnClickListener {
            val intent = Intent(requireContext(), AmigosActivity::class.java)
            startActivity(intent)
        }
    }

    private fun obtenerListaMonedas() {
        lifecycleScope.launch(Dispatchers.IO) {
            // llamada a la api
            val res = CryptoObject.getInstance()
                .create(ApiInterfaz::class.java)
                .getMarketData()
            withContext(Dispatchers.Main) {
                binding.recycler.layoutManager = LinearLayoutManager(requireContext())
                listaOriginal = res.body()!!.data.cryptoCurrencyList
                adapter = TopMarketAdapter(listaOriginal.toMutableList()) { crypto ->
                    verDetalle(crypto) // Llama a la función que muestra el detalle de la criptomoneda
                }
                binding.recycler.adapter = adapter
            }
        }
    }

    private fun configurarBuscador() {
        binding.svBuscadorNombre.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarLista(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText)
                return true
            }
        })
    }

    // Filtra la lista según el nombre (ignorando mayúsculas/minúsculas)
    private fun filtrarLista(query: String?) {
        val listaFiltrada = if (query.isNullOrEmpty()) {
            listaOriginal.toMutableList()
        } else {
            listaOriginal.filter { crypto ->
                crypto.name.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        adapter.actualizarLista(listaFiltrada)
    }

    private fun verDetalle(crypto: CryptoCurrency) {
        val intent = Intent(requireContext(), DetalleCryptoActivity::class.java)
        intent.putExtra("CRYPTO", crypto)
        startActivity(intent)
    }
}
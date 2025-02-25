package com.example.proyectoandroid.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.AmigosModel
import com.example.proyectoandroid.data.database.CrudAmigos
import com.example.proyectoandroid.databinding.ActivityAmigosBinding
import com.example.proyectoandroid.ui.adapter.AmigosAdapter

class AmigosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmigosBinding

    var lista = mutableListOf<AmigosModel>()
    private lateinit var adapter: AmigosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setRecycler()
        setListeners()
    }

    private fun setRecycler() {
        val layoutManger = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManger
        traerRegistros()
        adapter = AmigosAdapter(lista, { position -> borrarAmigo(position) }, { c -> update(c) })
        binding.recyclerView.adapter = adapter
    }

    private fun update(c: AmigosModel) {
        val i = Intent(this, AddAmigosActivity::class.java).apply {
            putExtra("amigos", c)
        }
        startActivity(i)
    }

    private fun borrarAmigo(p: Int) {
        val id = lista[p].id
        //Lo elimino de la lisa
        lista.removeAt(p)
        //lo elimino de la base de datos
        if (CrudAmigos().borrar(id)) {
            adapter.notifyItemRemoved(p)
        } else {
            Toast.makeText(this, "No se eliminó ningún registro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun traerRegistros() {
        lista = CrudAmigos().read()
        if (lista.size > 0) {
            binding.ivContactos.visibility = View.INVISIBLE
        } else {
            binding.ivContactos.visibility = View.VISIBLE
        }
    }

    private fun setListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddAmigosActivity::class.java))
        }
        binding.btSalirr.setOnClickListener {
            finish()
        }
    }

    override fun onRestart() {
        super.onRestart()
        setRecycler()
    }
}
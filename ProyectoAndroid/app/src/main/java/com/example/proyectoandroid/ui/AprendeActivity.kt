package com.example.proyectoandroid.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityAprendeBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.example.proyectoandroid.viewModel.AprendeViewModel

class AprendeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAprendeBinding

    private val aprendeViewModel: AprendeViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = Preferences(newBase)
        val idioma = prefs.getIdioma()
        val context = LocaleHelper.wrap(newBase, idioma)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAprendeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        aprendeViewModel.aprendeModel.observe(this, Observer {
            binding.tvTitulo.text = it.titulo
            binding.tvDescripcio.text = it.descripcion
        })
        setListeners()
    }

    private fun setListeners() {
        binding.constraint.setOnClickListener {
            aprendeViewModel.ramdomClick()
        }
        binding.btSalirA.setOnClickListener {
            finish()
        }
    }
}
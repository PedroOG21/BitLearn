package com.example.proyectoandroid.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityConfirmarDatosBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper

class ConfirmarDatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmarDatosBinding

    private var nombre = ""
    private var telefono = ""
    private var direccion = ""
    private var edad = 18

    override fun attachBaseContext(newBase: Context) {
        val prefs = Preferences(newBase)
        val idioma = prefs.getIdioma()
        val context = LocaleHelper.wrap(newBase, idioma)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityConfirmarDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recogerDatos()
        rellenarDatos()
        setListeners()
    }

    private fun recogerDatos() {
        val datos = intent.extras
        nombre = datos?.getString("nombre").toString()
        telefono = datos?.getString("telefono").toString()
        direccion = datos?.getString("direccion").toString()
        edad = datos?.getInt("edad")!!
    }

    private fun rellenarDatos() {
        binding.tvCnombre.text = nombre
        binding.tvCtelefono.text = telefono
        binding.tvCdireccion.text = direccion
        binding.tvCedad.text = edad.toString()
    }

    private fun setListeners() {
        binding.btAtras.setOnClickListener {
            finish()
        }
        binding.btConfirmar.setOnClickListener {
            // Guardardamos los datos
            val preferences = Preferences(this)
            preferences.saveUserData(
                nombre,
                telefono,
                direccion,
                edad,
                intent.getStringExtra("emailUsuarioLogeado").toString()
            )

            val resultIntent = Intent().apply {
                putExtra("nombre", nombre)
                putExtra("telefono", telefono)
                putExtra("direccion", direccion)
                putExtra("edad", edad)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }
}
package com.example.proyectoandroid.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.AmigosModel
import com.example.proyectoandroid.data.database.CrudAmigos
import com.example.proyectoandroid.databinding.ActivityAddAmigosBinding

class AddAmigosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAmigosBinding

    private var id = -1
    private var nombre = ""
    private var email = ""
    private var imagen = ""
    private var isUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recogerAmigos()
        setListeners()
    }

    private fun recogerAmigos() {
        val datos = intent.extras
        if (datos != null) {
            val c = datos.getSerializable("amigos") as AmigosModel
            isUpdate = true
            nombre = c.nombre
            imagen = c.imagen
            email = c.email
            id = c.id
            pintarDatos()
            binding.btAgregar.text = "Actualizar"
            binding.tvAgregarAmigo.text = "Actualizar Amigo"
            binding.etAcorreo.setEnabled(false);

        }

    }

    private fun pintarDatos() {
        binding.etAnombre.setText(nombre)
        binding.etAcorreo.setText(email)
    }

    private fun setListeners() {
        binding.imageView8.setOnClickListener {
            finish()
        }
        binding.btAgregar.setOnClickListener {
            guardarRegistro()
        }
    }

    private fun guardarRegistro() {
        if (datosCorrectos()) {
            imagen = "https://dummyimage.com/200x200/000/fff&text=" + (nombre.substring(
                0,
                1
            ) + email.substring(0, 2)).uppercase()
            val c = AmigosModel(id, nombre, email, imagen)
            if (!isUpdate) {
                if (CrudAmigos().create(c) != -1L) {
                    Toast.makeText(
                        this,
                        "Se ha añadido un registro a la agenda",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    binding.etAcorreo.error = "Email duplicado!!!!"
                }
            } else {
                if (CrudAmigos().update(c)) {
                    Toast.makeText(this, "Registro Editado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.etAcorreo.error = "Email duplicado!!!!"
                }
            }
        }

    }

    private fun datosCorrectos(): Boolean {
        nombre = binding.etAnombre.text.toString().trim()
        email = binding.etAcorreo.text.toString().trim()
        android.util.Log.d("AddAmigosActivity", "Email ingresado: $email")

        if (nombre.length < 3) {
            binding.etAnombre.error = "El campo nombre debe tener al menos 3 caracteres"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etAcorreo.error = "Debes introducir un email válido"
            return false
        }
        return true
    }

}
package com.example.proyectoandroid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityDatosBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.google.firebase.auth.FirebaseAuth

class DatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatosBinding

    private lateinit var preferences: Preferences

    private var nombre = ""
    private var telefono = ""
    private var direccion = ""
    private var edad = 18
    var emailUsuarioLogeado = ""
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        emailUsuarioLogeado = auth.currentUser?.email.toString()
        binding.tvCorreo.text = emailUsuarioLogeado
        binding.tvEdad.text = getString(R.string.tv_edad, edad)

        recuperarPreferends()
        setListeners()
    }

    private fun recuperarPreferends() {
        val preferences = Preferences(this)
        binding.etNombre.setText(preferences.getNombre())
        binding.etTelefono.setText(preferences.getTelefono())
        binding.etDireccion.setText(preferences.getDireccion())
        edad = preferences.getEdad()
        binding.sbEdad.progress = edad
        binding.tvEdad.text = getString(R.string.tv_edad, edad)

    }


    private fun setListeners() {
        binding.btSalirP.setOnClickListener {
            finish()
        }
        binding.btGuardar.setOnClickListener {
            if (comprobarCampos()) {
                enviarDatos()
            }
        }
        binding.sbEdad.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                edad = progress
                binding.tvEdad.text = getString(R.string.tv_edad, edad)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun enviarDatos() {
        val i = Intent(this, ConfirmarDatosActivity::class.java)
        val bundle = Bundle().apply {
            putString("nombre", nombre)
            putString("telefono", telefono)
            putString("direccion", direccion)
            putInt("edad", edad)
            putString("emailUsuarioLogeado", emailUsuarioLogeado)
        }
        i.putExtras(bundle)
        confirmarDatosLauncher.launch(i)
    }

    private val confirmarDatosLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                binding.etNombre.setText(data.getStringExtra("nombre"))
                binding.etTelefono.setText(data.getStringExtra("telefono"))
                binding.etDireccion.setText(data.getStringExtra("direccion"))
                val edadConfirmada = data.getIntExtra("edad", 18)
                binding.sbEdad.progress = edadConfirmada
                binding.tvEdad.text = edadConfirmada.toString()
            }
        }
    }

    private fun comprobarCampos(): Boolean {
        nombre = binding.etNombre.text.toString().trim()
        telefono = binding.etTelefono.text.toString().trim()
        direccion = binding.etDireccion.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.etNombre.error = ("El nombre no puede estar vacío")
            return false
        }
        if (telefono.isEmpty()) {
            binding.etTelefono.error = ("El teléfono no puede estar vacío")
            return false
        }
        if (direccion.isEmpty()) {
            binding.etDireccion.error = ("La dirección no puede estar vacía")
            return false
        }
        return true
    }

}
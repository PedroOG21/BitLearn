package com.example.proyectoandroid.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityDatosBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbPerfil: DatabaseReference

    private var nombre = ""
    private var telefono = ""
    private var direccion = ""
    private var edad = 0
    private var email = ""

    // ----------------------------------------------------------------------

    override fun attachBaseContext(newBase: Context) {
        val p = Preferences(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, p.getIdioma()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, getString(R.string.toast_no_usuario_logeado), Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }
        dbPerfil = FirebaseDatabase.getInstance()
            .reference.child("usuarios").child(userId).child("perfil")

        email = auth.currentUser?.email.orEmpty()
        binding.tvCorreo.text = email

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) binding.sbEdad.min = 0
        binding.sbEdad.max = 100
        binding.sbEdad.progress = edad
        binding.tvEdad2.text = getString(R.string.tv_edad, edad)

        binding.sbEdad.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, prog: Int, fromUser: Boolean) {
                edad = prog
                binding.tvEdad2.text = getString(R.string.tv_edad, edad)
            }

            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) = Unit
        })

        cargarPerfil()

        // Botones
        binding.btSalirP.setOnClickListener { finish() }
        binding.btGuardar.setOnClickListener {
            if (comprobarCampos()) confirmarDatosl()
        }
    }

    private fun cargarPerfil() {
        dbPerfil.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                if (!snap.exists()) return

                nombre = snap.child("nombre").getValue(String::class.java) ?: ""
                telefono = snap.child("telefono").getValue(String::class.java) ?: ""
                direccion = snap.child("direccion").getValue(String::class.java) ?: ""
                edad = snap.child("edad").getValue(Int::class.java) ?: 18
                email = snap.child("email").getValue(String::class.java) ?: email

                binding.etNombre.setText(nombre)
                binding.etTelefono.setText(telefono)
                binding.etDireccion.setText(direccion)
                binding.sbEdad.progress = edad
                binding.tvCorreo.text = email
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DatosActivity, R.string.error_usuario, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private fun comprobarCampos(): Boolean {
        nombre = binding.etNombre.text.toString().trim()
        telefono = binding.etTelefono.text.toString().trim()
        direccion = binding.etDireccion.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.etNombre.error = getString(R.string.error_nombre_vacio)
            return false
        }
        if (telefono.isEmpty()) {
            binding.etTelefono.error = getString(R.string.error_telefono_vacio)
            return false
        }
        if (direccion.isEmpty()) {
            binding.etDireccion.error = getString(R.string.error_direccion_vacia)
            return false
        }
        return true
    }


    private fun confirmarDatosl() {
        val i = Intent(this, ConfirmarDatosActivity::class.java).apply {
            putExtra("nombre", nombre)
            putExtra("telefono", telefono)
            putExtra("direccion", direccion)
            putExtra("edad", edad)
            putExtra("emailUsuarioLogeado", email)
        }
        confirmarLauncher.launch(i)
    }

    private val confirmarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        result.data?.let { data ->
            nombre = data.getStringExtra("nombre") ?: nombre
            telefono = data.getStringExtra("telefono") ?: telefono
            direccion = data.getStringExtra("direccion") ?: direccion
            edad = data.getIntExtra("edad", edad)

            binding.etNombre.setText(nombre)
            binding.etTelefono.setText(telefono)
            binding.etDireccion.setText(direccion)
            binding.sbEdad.progress = edad

            val perfilMap = mapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "direccion" to direccion,
                "edad" to edad,
                "email" to email
            )
            dbPerfil.setValue(perfilMap)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_perfil_guardado),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_error_guardar_perfil),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}

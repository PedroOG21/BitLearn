package com.example.proyectoandroid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.whenCreated
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityMenuBinding
import com.example.proyectoandroid.fragments.Fragment_chat
import com.example.proyectoandroid.fragments.Fragment_crypto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth

        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.setPersistenceEnabled(true)

        setListeners()
    }

    private fun setListeners() {
        binding.navegation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_salir -> {
                    finishAffinity()
                    true
                }

                R.id.item_logout -> {
                    auth.signOut()
                    finish()
                    true
                }

                R.id.item_informacion -> {
                    startActivity(Intent(this, InformacionActivity::class.java))
                    true
                }

                R.id.item_perfil -> {
                    startActivity(Intent(this, DatosActivity::class.java))
                    true
                }

                R.id.item_configuracion -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    true
                }

                else -> true
            }
        }
        binding.btChat.setOnClickListener {
            // Cargar el fragment
            val fragment = Fragment_chat()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fg_contenedor, fragment)
                .commit()
        }
        binding.btCrypto.setOnClickListener {
            // Cargar el fragment
            val fragment = Fragment_crypto()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fg_contenedor, fragment)
                .commit()
        }
        binding.btRanking.setOnClickListener {
            Toast.makeText(this, "Próximamente", Toast.LENGTH_SHORT).show()
        }
    }
}
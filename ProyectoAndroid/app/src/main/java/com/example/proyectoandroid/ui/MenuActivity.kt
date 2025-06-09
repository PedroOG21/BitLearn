package com.example.proyectoandroid.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.SolicitudModel
import com.example.proyectoandroid.data.net.NotificacionHelper
import com.example.proyectoandroid.databinding.ActivityMenuBinding
import com.example.proyectoandroid.fragments.Fragment_chat
import com.example.proyectoandroid.fragments.Fragment_crypto
import com.example.proyectoandroid.fragments.Fragment_ranking
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var solicitudesRef: DatabaseReference


    private var cargarIniciales = false

    override fun attachBaseContext(newBase: Context) {
        val prefs = Preferences(newBase)
        val idioma = prefs.getIdioma()
        val context = LocaleHelper.wrap(newBase, idioma)
        super.attachBaseContext(context)
    }

    private val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                comprobarNotificacionesActivas()
            } else {
                justificacionNotificacion()
            }
        }

    private fun gestionarNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    comprobarNotificacionesActivas()
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    justificacionNotificacion()
                }

                else -> {
                    notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            comprobarNotificacionesActivas()
        }
    }

    private fun comprobarNotificacionesActivas() {
        val notificacionActivadas = NotificationManagerCompat.from(this).areNotificationsEnabled()
        if (!notificacionActivadas) {
            // Volver a preguntar
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.notificaciones_off_titulo))
                .setMessage(getString(R.string.notificaciones_off_mensaje))
                .setPositiveButton(getString(R.string.notificaciones_off_ajustes)) { _, _ ->
                    abrirAjustesTelefono()
                }
                .setNegativeButton(getString(R.string.notificaciones_off_cancelar), null)
                .setCancelable(false)
                .show()
        }
    }

    private fun justificacionNotificacion() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permiso_notificaciones_titulo))
            .setMessage(getString(R.string.permiso_notificaciones_mensaje))
            .setPositiveButton(getString(R.string.permiso_notificaciones_ajustes)) { _, _ ->
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(getString(R.string.permiso_notificaciones_cancelar), null)
            .setCancelable(false)
            .show()
    }

    // Vamos a los ajustes a dar permiso
    private fun abrirAjustesTelefono() {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            // Para Android 5–7:
            putExtra("app_package", packageName)
            putExtra("app_uid", applicationInfo.uid)
            // Para Android 8+:
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = Preferences(this)
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

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
        val uid = auth.currentUser?.uid ?: return
        solicitudesRef = FirebaseDatabase
            .getInstance()
            .reference
            .child("usuarios")
            .child(uid)
            .child("solicitudes")

        verNotificaciones()
        setListeners()
        gestionarNotificaciones()


    }

    override fun onResume() {
        super.onResume()

        val prefs = Preferences(this)
        if (prefs.haCambiadoIdioma()) {
            prefs.limpiarCambioIdioma()
            recreate()
        }
    }

    private fun verNotificaciones() {
        solicitudesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cargarIniciales = true
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        solicitudesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snap: DataSnapshot, previousChildName: String?) {
                if (!cargarIniciales) return
                val sol = snap.getValue(SolicitudModel::class.java) ?: return
                NotificacionHelper.mostrarNotificacion(this@MenuActivity, sol.fromNombre)
            }

            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, prev: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
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
            val fragment = Fragment_chat()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fg_contenedor, fragment)
                .commit()
        }
        binding.btCrypto.setOnClickListener {
            val fragment = Fragment_crypto()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fg_contenedor, fragment)
                .commit()
        }
        binding.btRanking.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fg_contenedor, Fragment_ranking())
                .commit()
        }
    }
}
package com.example.proyectoandroid.ui

import android.Manifest
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityConfiguracionBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences

class ConfiguracionActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityConfiguracionBinding

    private lateinit var preferences: Preferences

    private val REQUEST_ACTIVITY_RECOGNITION = 1001
    private lateinit var sensorManager: SensorManager
    private var sensorPasos: Sensor? = null

    private var pasosInicial: Float? = null

    private val activityRecognitionPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permiso ->
            if (permiso == true) {
                inicializarSensores()
            } else {
                Toast.makeText(
                    this,
                    "El usuario denegó el permiso de actividad física.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        preferences = Preferences(this)

        permisosDeporte()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        inicializarSensores()

        recordarTema()

        setListeners()

    }

    private fun inicializarSensores() {
        sensorPasos = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensorPasos == null) {
            Toast.makeText(
                this,
                "Sensor de pasos no disponible en este dispositivo",
                Toast.LENGTH_LONG
            ).show()
            binding.tvPasos.text = "0"
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorPasos?.let { ponerListenersSensores(it) }
    }

    private fun permisosDeporte() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        ) {
            mostrarExplicacion()
        } else {
            escogerPermisos() //intent
        }
    }

    private fun escogerPermisos() {
        activityRecognitionPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    private fun mostrarExplicacion() {
        AlertDialog.Builder(this)
            .setTitle("Permiso de Actividad Física")
            .setMessage("Para el uso adecuado de esta aplicación es necesario aceptar el permiso de reconocimiento de actividad física.")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS))
            }
            .create()
            .show()
    }

    private fun ponerListenersSensores(sensor: Sensor?) {
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun pintarValores(tv: TextView, valor: Float) {
        tv.text = String.format("%.0f", valor)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                if (pasosInicial == null) {
                    pasosInicial = preferences.getPasosIniciales()
                    if (pasosInicial == null) {
                        pasosInicial = it.values[0]
                        preferences.setPasosIniciales(pasosInicial!!)
                    }
                }
                val pasosRecorridos = it.values[0] + (pasosInicial ?: 0f)
                pintarValores(binding.tvPasos, pasosRecorridos)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // ------------------------------------------------------------------------------------

    private fun ponerTema(mode: Int) {
        if (mode == 0) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setListeners() {
        binding.btSalirAjustes.setOnClickListener {
            finish()
        }

        binding.swTema.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ponerTema(1)
                preferences.setDarkMode(false)
            } else {
                ponerTema(0)
                preferences.setDarkMode(true)
            }
        }
    }

    private fun recordarTema() {
        if (preferences.isDarkMode()) {
            ponerTema(0) // Modo oscuro
            binding.swTema.isChecked = false
            binding.swTema.setText("Modo oscuro")
        } else {
            ponerTema(1) // Modo claro
            binding.swTema.isChecked = true
            binding.swTema.setText("Modo claro")
        }
    }

}


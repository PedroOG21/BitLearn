package com.example.proyectoandroid.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityConfiguracionBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.Locale

class ConfiguracionActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityConfiguracionBinding
    private lateinit var prefs: Preferences
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sensorManager: SensorManager
    private var pasosSensor: Sensor? = null
    private var sensorDesp = -1f
    private var pasosIniciales = 0
    private var firebase = false

    private lateinit var listenerTema: CompoundButton.OnCheckedChangeListener
    private lateinit var cargarTemaDefecto: AdapterView.OnItemSelectedListener

    private val hoyId: String
        get() = LocalDate.now().toString()

    override fun attachBaseContext(newBase: Context) {
        prefs = Preferences(newBase)
        val lang = prefs.getIdioma() ?: "es"
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Preferences(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        cambiarIdioma(prefs.getIdioma())
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        auth = Firebase.auth
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pasosSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (pasosSensor == null) {
            toast(R.string.sensor_no_disponible)
            binding.tvPasos.text = "0"
        }

        binding.btSalirAjustes.setOnClickListener { finish() }
        cambioTema()
        ponerIdioma()
        solicitarPermisoPasos()
        setListeners()
    }

    private fun setListeners() {
        // Aviso legal: al pulsar el TextView, mostramos un AlertDialog
        binding.tvAvisolegal.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.aviso_legal_titulo))
                .setMessage(getString(R.string.aviso_legal_texto))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun solicitarPermisoPasos() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> cargarPasosIniciales()

            shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION) ->
                justificacionPermiso()

            else -> permisosLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private val permisosLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) cargarPasosIniciales() else toast(R.string.permiso_denegado)
        }

    private fun justificacionPermiso() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permiso_actividad)
            .setMessage(R.string.permiso_actividad_mensaje)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.aceptar) { _, _ ->
                permisosLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            .setCancelable(false)
            .show()
    }

    private fun cargarPasosIniciales() {
        val uid = auth.currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance()
            .reference
            .child("usuarios")
            .child(uid)

        database.child("resumen").child("pasos_totales")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pasosIniciales = snapshot.getValue(Int::class.java) ?: 0
                    binding.tvPasos.text = pasosIniciales.toString()

                    val monedasInicial = pasosIniciales / 500.0
                    binding.etSueldo.setText("%.2f".format(monedasInicial))

                    firebase = true
                    sensorManager.registerListener(
                        this@ConfiguracionActivity,
                        pasosSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )

                    database.child("resumen").child("monedero")
                        .addValueEventListener(object : ValueEventListener {

                            override fun onDataChange(monedaSnapshot: DataSnapshot) {
                                // Si alguien desde Firebase modifica, actualizamos el EditText:
                                val monedaBD = monedaSnapshot.getValue(Double::class.java) ?: 0.0
                                binding.etSueldo.setText("%.2f".format(monedaBD))
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    toast(R.string.error_cargar_pasos)
                    firebase = true
                    sensorManager.registerListener(
                        this@ConfiguracionActivity,
                        pasosSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            })
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER || !firebase) return
        val current = event.values[0]
        if (sensorDesp < 0) {
            sensorDesp = current
            return
        }
        val ganadosDesdeInicio = (current - sensorDesp).toInt()
        val total = pasosIniciales + ganadosDesdeInicio
        binding.tvPasos.text = total.toString()
        guardarFirebase(total, ganadosDesdeInicio)
    }

    private fun guardarFirebase(totalSteps: Int, dailySteps: Int) {
        val monedas = totalSteps / 500.0
        val tiempo = System.currentTimeMillis()

        val actualizarResumen = mapOf<String, Any>(
            "pasos_totales" to totalSteps,
            "monedero" to monedas,
            "ultima_actualizacion" to tiempo
        )
        database.child("resumen")
            .updateChildren(actualizarResumen)
            .addOnFailureListener { toast(R.string.error_guardar_pasos) }

        database.child("historial")
            .child(hoyId)
            .child("pasos")
            .setValue(dailySteps)

        binding.etSueldo.setText("%.2f".format(monedas))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    private fun cambioTema() {
        val oscuro = prefs.isDarkMode()
        binding.swTema.isChecked = oscuro
        binding.swTema.text =
            if (oscuro) getString(R.string.modo_oscuro) else getString(R.string.modo_claro)

        listenerTema = CompoundButton.OnCheckedChangeListener { btn, checked ->
            dialogoReinicio(
                onConfirm = {
                    prefs.setDarkMode(checked)
                    finishAffinity()
                },
                onCancel = {
                    btn.setOnCheckedChangeListener(null)
                    val prev = prefs.isDarkMode()
                    btn.isChecked = prev
                    btn.text =
                        if (prev) getString(R.string.modo_oscuro) else getString(R.string.modo_claro)
                    btn.setOnCheckedChangeListener(listenerTema)
                }
            )
        }
        binding.swTema.setOnCheckedChangeListener(listenerTema)
    }

    private fun ponerIdioma() {
        val labels = listOf(
            getString(R.string.espanol),
            getString(R.string.ingles),
            getString(R.string.frances)
        )
        val codigoIdiomas = listOf("es", "en", "fr")
        binding.spIdioma.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, labels).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        val idiomaInicial = prefs.getIdioma() ?: "es"
        binding.spIdioma.setSelection(codigoIdiomas.indexOf(idiomaInicial))

        cargarTemaDefecto = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val newLang = codigoIdiomas[pos]
                if (newLang != idiomaInicial) {
                    dialogoReinicio(
                        onConfirm = {
                            prefs.setIdioma(newLang)
                            finishAffinity()
                        },
                        onCancel = {
                            binding.spIdioma.onItemSelectedListener = null
                            binding.spIdioma.setSelection(codigoIdiomas.indexOf(idiomaInicial))
                            binding.spIdioma.onItemSelectedListener = cargarTemaDefecto
                        }
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spIdioma.onItemSelectedListener = cargarTemaDefecto
    }

    private fun dialogoReinicio(onConfirm: () -> Unit, onCancel: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.reiniciar_app)
            .setMessage(R.string.reiniciar_mensaje)
            .setPositiveButton(R.string.salir) { _, _ -> onConfirm() }
            .setNegativeButton(R.string.cancelar) { _, _ -> onCancel() }
            .setCancelable(false)
            .show()
    }

    private fun cambiarIdioma(languageCode: String?) {
        languageCode ?: return
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        resources.configuration.setLocale(locale)
        resources.configuration.setLayoutDirection(locale)
        resources.updateConfiguration(resources.configuration, resources.displayMetrics)
    }

    private fun toast(resId: Int) =
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
}

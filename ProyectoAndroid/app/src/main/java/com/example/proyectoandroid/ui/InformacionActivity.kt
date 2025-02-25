package com.example.proyectoandroid.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityInformacionBinding
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class InformacionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityInformacionBinding
    private lateinit var mediaController: MediaController

    private lateinit var mapa: GoogleMap
    private val LOCATION_CODE = 1000

    //CUANDO ENTRAS POR PRIMERA VEZ EL TLF PREGUNTA QUE SI QUIERES O NO PERMITIR EL PERMISO
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            if (
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true
                ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                gestionarLocalizacion()
            } else {
                Toast.makeText(this, "El usuario denego el permiso...", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityInformacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mediaController = MediaController(this)

        iniciarMapa()
        inicializarWebView()
        setListeners()

    }

    private fun setListeners() {
        binding.swipe.setOnRefreshListener {
            binding.webView.reload()
        }
        binding.btSalirAjustes2.setOnClickListener {
            finish()
        }
    }

    private fun inicializarWebView() {
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.swipe.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.swipe.isRefreshing = false
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl("https://coinmarketcap.com/es/")
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun iniciarMapa() {
        val fragment = SupportMapFragment()
        fragment.getMapAsync(this)
        supportFragmentManager.commit {
            //siempre se pone a true
            setReorderingAllowed(true)
            //cargamos el fragmet
            add(R.id.mapFragment, fragment)
        }
    }


    private fun gestionarLocalizacion() {
        if (!::mapa.isInitialized) return // sirve para saber si esta inicializada
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED // el Manifest de android
            &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mapa.isMyLocationEnabled = true
            mapa.uiSettings.isMyLocationButtonEnabled = true
        } else {
            pedirPermisos()
        }
    }

    private fun pedirPermisos() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            mostrarExplicacion()
        } else {
            escogerPermisos() //intent
        }
    }

    private fun escogerPermisos() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun mostrarExplicacion() {
        AlertDialog.Builder(this)
            .setTitle("Permisos de Ubicación")
            .setMessage("Para el uso adecuado de esta aplicación es necesario aceptar el permiso de ubicación.")
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

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        // Configuración de la interfaz del mapa
        mapa.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true// zoom (pinch)
            isScrollGesturesEnabled = true// gesto de desplazamiento
            isRotateGesturesEnabled = true// gesto de rotación
        }

        // Tipo de mapa y características adicionales
        mapa.mapType = GoogleMap.MAP_TYPE_NORMAL
        mapa.isBuildingsEnabled = true// mostrar edificios en 3D
        mapa.isIndoorEnabled = true // habilitar mapas interiores si están disponibles

        // Agregar marcadores repartidos por España
        ponerMarcadoresEnEspaña()

        mostrarAnimacion(LatLng(36.8400, -2.4700))

        gestionarLocalizacion()
    }

    //PONER MARCADORES
    private fun ponerMarcador(coordenada: LatLng) {
        val market = MarkerOptions().position(coordenada).title("BANCO")
        mapa.addMarker(market)
    }

    private fun ponerMarcadoresEnEspaña() {
        // Banco en Madrid
        ponerMarcador(LatLng(40.4168, -3.7038))
        // Banco en Barcelona
        ponerMarcador(LatLng(41.3851, 2.1734))
        // Banco en Valencia
        ponerMarcador(LatLng(39.4699, -0.3763))
        // Banco en Sevilla
        ponerMarcador(LatLng(37.3891, -5.9845))
        // Banco en Zaragoza
        ponerMarcador(LatLng(41.6488, -0.8891))
        // Banco en Málaga
        ponerMarcador(LatLng(36.7213, -4.4214))
        // Banco en Murcia
        ponerMarcador(LatLng(37.9922, -1.1307))
        // Banco en Palma de Mallorca
        ponerMarcador(LatLng(39.5696, 2.6502))
        // Banco en Bilbao
        ponerMarcador(LatLng(43.2630, -2.9350))
        // Banco en Alicante
        ponerMarcador(LatLng(38.3452, -0.4810))
        // Banco en Córdoba
        ponerMarcador(LatLng(37.8882, -4.7794))
        // Banco en Valladolid
        ponerMarcador(LatLng(41.6523, -4.7286))
        // Banco en Vigo
        ponerMarcador(LatLng(42.2406, -8.7207))
        // Banco en Gijón
        ponerMarcador(LatLng(43.5350, -5.6611))
        // Banco en Granada
        ponerMarcador(LatLng(37.1773, -3.5986))
        // Banco en A Coruña
        ponerMarcador(LatLng(43.3623, -8.4115))
        // Banco en Oviedo
        ponerMarcador(LatLng(43.3619, -5.8494))
        // Banco en Santa Cruz de Tenerife
        ponerMarcador(LatLng(28.4636, -16.2546))
        // Banco en Santander
        ponerMarcador(LatLng(43.4603, -3.8107))
        // Banco en Almería
        ponerMarcador(LatLng(36.8400, -2.4700))
    }

    //ANIMACION DE CAMARA ACERCANDOSE
    private fun mostrarAnimacion(coordenada: LatLng) {
        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordenada, 12f),
            3500, //segundos
            null
        )
    }

    override fun onRestart() {
        super.onRestart()
        gestionarLocalizacion()
    }


    // ---------------------------------------------------------------------------------------------
    private fun reproducirVideo() {
        // Construir la URI correctamente
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.video}")
        binding.videoView.setVideoURI(uri)

        // Configurar el MediaController para que aparezca el menú típico
        binding.videoView.setMediaController(mediaController)
        mediaController.setAnchorView(binding.videoView)

        // Solicitar el foco y comenzar la reproducción
        binding.videoView.requestFocus()
        binding.videoView.start()
    }

    override fun onResume() {
        super.onResume()
        reproducirVideo()
    }

}

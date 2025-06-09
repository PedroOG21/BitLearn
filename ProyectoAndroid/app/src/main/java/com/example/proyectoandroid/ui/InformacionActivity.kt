package com.example.proyectoandroid.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.ActivityInformacionBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
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


    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                gestionarLocalizacion()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.toast_permiso_denegado_ubicacion),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun attachBaseContext(newBase: Context) {
        val prefs = Preferences(newBase)
        val idioma = prefs.getIdioma()
        super.attachBaseContext(LocaleHelper.wrap(newBase, idioma))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityInformacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        mediaController = MediaController(this)

        iniciarMapa()
        inicializarWebView()
        setListeners()
    }

    private fun setListeners() {
        binding.swipe.setOnRefreshListener { binding.webView.reload() }
        binding.btSalirAjustes2.setOnClickListener { finish() }
    }

    private fun inicializarWebView() {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?, request: WebResourceRequest?
                ) = false

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    binding.swipe.isRefreshing = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.swipe.isRefreshing = false
                }
            }
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            loadUrl("https://coinmarketcap.com/es/")
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack() else super.onBackPressed()
    }

    private fun iniciarMapa() {
        val fragment = SupportMapFragment()
        fragment.getMapAsync(this)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.mapFragment, fragment)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        mapa.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        mapa.mapType = GoogleMap.MAP_TYPE_NORMAL
        mapa.isBuildingsEnabled = true
        mapa.isIndoorEnabled = true

        ponerMarcadoresEnEspaña()
        mostrarAnimacion(LatLng(36.84, -2.47))
        gestionarLocalizacion()
    }

    private fun gestionarLocalizacion() {
        if (!::mapa.isInitialized) return
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted && coarseGranted) {
            mapa.isMyLocationEnabled = true
            mapa.uiSettings.isMyLocationButtonEnabled = true
        } else {
            pedirPermisos()
        }
    }

    private fun pedirPermisos() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            mostrarExplicacion()
        } else {
            escogerPermisos()
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
            .setTitle(getString(R.string.dialog_permiso_ubicacion_titulo))
            .setMessage(getString(R.string.dialog_permiso_ubicacion_mensaje))
            .setNegativeButton(getString(R.string.cancelar), null)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.aceptar)) { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS))
            }
            .show()
    }

    private fun ponerMarcador(coord: LatLng) {
        mapa.addMarker(
            MarkerOptions()
                .position(coord)
                .title(getString(R.string.marcador_banco))
        )
    }

    private fun ponerMarcadoresEnEspaña() {
        ponerMarcador(LatLng(40.4168, -3.7038))  // Madrid
        ponerMarcador(LatLng(41.3851, 2.1734))  // Barcelona
        ponerMarcador(LatLng(39.4699, -0.3763))  // Valencia
        ponerMarcador(LatLng(37.3891, -5.9845))  // Sevilla
        ponerMarcador(LatLng(41.6488, -0.8891))  // Zaragoza
        ponerMarcador(LatLng(36.7213, -4.4214))  // Málaga
        ponerMarcador(LatLng(37.9922, -1.1307))  // Murcia
        ponerMarcador(LatLng(39.5696, 2.6502))  // Palma
        ponerMarcador(LatLng(43.263, -2.9350))  // Bilbao
        ponerMarcador(LatLng(38.3452, -0.4810))  // Alicante
        ponerMarcador(LatLng(37.8882, -4.7794))  // Córdoba
        ponerMarcador(LatLng(41.6523, -4.7286))  // Valladolid
        ponerMarcador(LatLng(42.2406, -8.7207))  // Vigo
        ponerMarcador(LatLng(43.535, -5.6611))  // Gijón
        ponerMarcador(LatLng(37.1773, -3.5986))  // Granada
        ponerMarcador(LatLng(43.3623, -8.4115))  // A Coruña
        ponerMarcador(LatLng(43.3619, -5.8494))  // Oviedo
        ponerMarcador(LatLng(28.4636, -16.2546))  // Tenerife
        ponerMarcador(LatLng(43.4603, -3.8107))  // Santander
        ponerMarcador(LatLng(36.84, -2.47))  // Almería
    }

    private fun mostrarAnimacion(coord: LatLng) {
        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 12f), 3500, null)
    }

    private fun reproducirVideo() {
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.video}")
        binding.videoView.apply {
            setVideoURI(uri)
            setMediaController(mediaController)
            mediaController.setAnchorView(this)
            requestFocus()
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        reproducirVideo()
    }

    override fun onRestart() {
        super.onRestart()
        gestionarLocalizacion()
    }
}

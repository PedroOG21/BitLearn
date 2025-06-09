package com.example.proyectoandroid.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.SolicitudModel
import com.example.proyectoandroid.databinding.ActivitySolicitudesBinding
import com.example.proyectoandroid.ui.adapter.SolicitudesAdapter
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SolicitudesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var prefs: Preferences
    private val lista = mutableListOf<SolicitudModel>()
    private lateinit var adapter: SolicitudesAdapter

    override fun attachBaseContext(newBase: Context) {
        prefs = Preferences(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, prefs.getIdioma()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySolicitudesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        auth  = FirebaseAuth.getInstance()
        prefs = Preferences(this)
        dbRef = FirebaseDatabase.getInstance()
            .reference
            .child("usuarios")
            .child(auth.currentUser!!.uid)

        adapter = SolicitudesAdapter(lista,
            onAccept = { aceptarSolicitud(it) },
            onReject = { rechazarSolicitud(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        dbRef.child("solicitudes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    for (i in snapshot.children) {
                        val sol = i.getValue(SolicitudModel::class.java) ?: continue
                        sol.id = i.key!!
                        lista.add(sol)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SolicitudesActivity,
                        getString(R.string.error_buscar_usuario),
                        Toast.LENGTH_SHORT).show()
                }
            })
        setListeners()

    }

    private fun setListeners() {
        binding.btAtras2.setOnClickListener{
            finish()
        }
    }


    private fun rechazarSolicitud(sol: SolicitudModel) {
        dbRef.child("solicitudes")
            .child(sol.id)
            .removeValue()
    }

    private fun aceptarSolicitud(sol: SolicitudModel) {
        val miId     = auth.currentUser!!.uid
        val amigoId = sol.fromUid
        val tiempo = ServerValue.TIMESTAMP

        val miNombre = prefs.getPerfilNombre()
        val miEmail  = auth.currentUser!!.email ?: ""
        val miImagen = prefs.getPerfilImagen()

        val actualizar = mutableMapOf<String, Any?>(
            "/usuarios/$miId/amigos/$amigoId" to mapOf(
                "id"           to amigoId,
                "nombre"       to sol.fromNombre,
                "email"        to sol.fromEmail,
                "imagen"       to sol.fromImagen,
                "fechaAmistad" to tiempo
            ),
            // Él/ella añade a su lista de amigos
            "/usuarios/$amigoId/amigos/$miId" to mapOf(
                "id"           to miId,
                "nombre"       to miNombre,
                "email"        to miEmail,
                "imagen"       to miImagen,
                "fechaAmistad" to tiempo
            ),
            // Y elimino la solicitud original
            "/usuarios/$miId/solicitudes/${sol.id}" to null
        )

        // Hacer la operacion
        FirebaseDatabase.getInstance().reference
            .updateChildren(actualizar)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud aceptada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show()
            }
    }
}

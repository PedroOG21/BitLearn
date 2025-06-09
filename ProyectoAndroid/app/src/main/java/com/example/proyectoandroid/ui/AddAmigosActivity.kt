package com.example.proyectoandroid.ui

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.AmigosModel
import com.example.proyectoandroid.data.database.CrudAmigos
import com.example.proyectoandroid.databinding.ActivityAddAmigosBinding
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddAmigosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAmigosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var prefs: Preferences

    private var isUpdate = false
    private lateinit var amigo: AmigosModel

    override fun attachBaseContext(newBase: Context) {
        prefs = Preferences(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, prefs.getIdioma()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference
        prefs = Preferences(this)

        // Si viene un "amigo" para actualizar, configuramos el modo edición
        intent.getSerializableExtra("amigo")?.let {
            amigo = it as AmigosModel
            isUpdate = true
            binding.etAcorreo.setText(amigo.email)
            binding.etAcorreo.isEnabled = false
            binding.btAgregar.text = getString(R.string.actualizar)
            binding.tvAgregarAmigo.text = getString(R.string.actualizar_amigo)
        }

        binding.imageView8.setOnClickListener { finish() }
        binding.btAgregar.setOnClickListener {
            if (isUpdate) actualizarAmigo()
            else enviarSolicitud()
        }
    }

    private fun enviarSolicitud() {
        val emailDestino = binding.etAcorreo.text.toString().trim()
        val currentUser = auth.currentUser
        val emailPerfil = currentUser?.email ?: ""

        if (!Patterns.EMAIL_ADDRESS.matcher(emailDestino).matches()) {
            binding.etAcorreo.error = getString(R.string.error_email_valido)
            return
        }

        // Evitar agregarse a uno mismo
        if (emailDestino.equals(emailPerfil, ignoreCase = true)) {
            Toast.makeText(this, getString(R.string.error_autoinvitacion), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val nombrePerfil = prefs.getPerfilNombre()
        val imagen = "https://dummyimage.com/200x200/000/fff&text=" +
                (nombrePerfil.take(1) + emailPerfil.take(2)).uppercase()
        prefs.setPerfilImagen(imagen)

        dbRef.child("usuarios")
            .orderByChild("perfil/email")
            .equalTo(emailDestino)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    if (!snap.exists()) {
                        binding.etAcorreo.error =
                            getString(R.string.error_usuario_no_encontrado)
                        return
                    }
                    val child = snap.children.first()
                    val friendUid = child.key!!

                    // Volver a comprobar que no sea uno mismo
                    val miID = currentUser!!.uid
                    if (friendUid == miID) {
                        Toast.makeText(
                            this@AddAmigosActivity,
                            getString(R.string.error_autoinvitacion),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // Verificar si ya existe una solicitud pendiente a este friendUid
                    val solicitudesRef = dbRef.child("usuarios")
                        .child(friendUid)
                        .child("solicitudes")
                    solicitudesRef
                        .orderByChild("fromUid")
                        .equalTo(miID)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(solicitudesSnap: DataSnapshot) {
                                if (solicitudesSnap.exists()) {
                                    // Ya había una solicitud pendiente
                                    Toast.makeText(
                                        this@AddAmigosActivity,
                                        getString(R.string.error_solicitud_ya_enviada),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return
                                }

                                // Preparar la solicitud
                                val solicitud = mapOf<String, Any>(
                                    "fromUid" to miID,
                                    "fromNombre" to nombrePerfil,
                                    "fromEmail" to emailPerfil,
                                    "fromImagen" to imagen,
                                    "timestamp" to ServerValue.TIMESTAMP
                                )
                                val solicitarId = dbRef.push().key!!

                                val actualizar = hashMapOf<String, Any>(
                                    "/usuarios/$friendUid/solicitudes/$solicitarId" to solicitud,
                                    "/usuarios/$miID/enviados/$solicitarId" to true
                                )
                                dbRef.updateChildren(actualizar)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@AddAmigosActivity,
                                            getString(R.string.solicitud_enviada),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this@AddAmigosActivity,
                                            getString(R.string.error_solicitud_enviar),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@AddAmigosActivity,
                                    getString(R.string.error_buscar_usuario),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                override fun onCancelled(e: DatabaseError) {
                    Toast.makeText(
                        this@AddAmigosActivity,
                        getString(R.string.error_buscar_usuario),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun actualizarAmigo() {
        CrudAmigos().actualizar(amigo)
            .addOnSuccessListener {
                Toast.makeText(
                    this, getString(R.string.amigo_actualizado),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this, getString(R.string.error_actualizar_amigo),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

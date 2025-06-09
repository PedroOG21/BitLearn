package com.example.proyectoandroid.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.R
import com.example.proyectoandroid.data.AmigosModel
import com.example.proyectoandroid.databinding.ActivityAmigosBinding
import com.example.proyectoandroid.ui.adapter.AmigosAdapter
import com.example.proyectoandroid.ui.prefenrences.Preferences
import com.example.proyectoandroid.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AmigosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmigosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: Preferences
    private lateinit var adapter: AmigosAdapter
    private val lista = mutableListOf<AmigosModel>()
    private lateinit var dbRef: DatabaseReference

    override fun attachBaseContext(newBase: Context) {
        prefs = Preferences(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, prefs.getIdioma()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAmigosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        prefs = Preferences(this)
        val userId = auth.currentUser?.uid ?: return
        dbRef = FirebaseDatabase.getInstance()
            .reference.child("usuarios").child(userId).child("amigos")

        setupRecycler()
        setListeners()
    }

    private fun setupRecycler() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AmigosAdapter(
            lista,
            onDelete = { amigo -> borrarAmigo(amigo) },
            onEdit = { amigo -> editarAmigo(amigo) }
        )
        binding.recyclerView.adapter = adapter
        cargarAmigos()
    }

    private fun cargarAmigos() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista.clear()
                for (ch in snapshot.children) {
                    val amigo = ch.getValue(AmigosModel::class.java) ?: continue
                    amigo.id = ch.key!!
                    lista.add(amigo)
                }
                adapter.notifyDataSetChanged()
                binding.ivContactos.visibility =
                    if (lista.isEmpty()) View.VISIBLE else View.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun borrarAmigo(amigo: AmigosModel) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_eliminar_amigo_titulo))
            .setMessage(getString(R.string.dialog_eliminar_amigo_mensaje))
            .setPositiveButton(getString(R.string.dialog_eliminar_amigo_confirmar)) { _, _ ->
                val myUid = auth.currentUser!!.uid
                val amigoId = amigo.id
                val actualizacion = mapOf<String, Any?>(
                    "/usuarios/$myUid/amigos/$amigoId" to null,
                    "/usuarios/$amigoId/amigos/$myUid" to null
                )
                FirebaseDatabase.getInstance().reference
                    .updateChildren(actualizacion)
            }
            .setNegativeButton(getString(R.string.dialog_eliminar_amigo_cancelar), null)
            .show()
    }

    private fun editarAmigo(amigo: AmigosModel) {
        startActivity(Intent(this, AddAmigosActivity::class.java).apply {
            putExtra("amigo", amigo)
        })
    }

    private fun setListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddAmigosActivity::class.java))
        }
        binding.btSalirr.setOnClickListener {
            finish()
        }
        binding.btnSolicitudes.setOnClickListener {
            startActivity(Intent(this, SolicitudesActivity::class.java))
        }
    }
}

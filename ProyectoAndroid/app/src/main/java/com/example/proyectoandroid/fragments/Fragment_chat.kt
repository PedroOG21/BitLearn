package com.example.proyectoandroid.fragments

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.FragmentChatBinding
import com.example.proyectoandroid.domain.models.ChatModel
import com.example.proyectoandroid.ui.adapter.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class Fragment_chat : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var adapter: ChatAdapter

    private var emailUsuarioLogeado = ""
    private val listadoChats = mutableListOf<ChatModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        emailUsuarioLogeado = auth.currentUser?.email.toString()

        databaseRef = FirebaseDatabase.getInstance().getReference("chat")

        setRecycler()
        setListeners()
        return binding.root
    }

    private fun setRecycler() {
        binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatAdapter(listadoChats, emailUsuarioLogeado) { mensaje ->
            eliminarMensaje(mensaje)
        }
        binding.rvChats.adapter = adapter
    }

    private fun eliminarMensaje(mensaje: ChatModel) {
        databaseRef.orderByChild("fecha")
            .equalTo(mensaje.fecha.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (nodo in snapshot.children) {
                        nodo.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.toast_mensaje_eliminado),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.toast_error_eliminar_mensaje),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_error_base_datos),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setListeners() {
        binding.imageView.setOnClickListener { enviar() }

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listadoChats.clear()
                for (nodo in snapshot.children) {
                    val chatNodo = nodo.getValue(ChatModel::class.java)
                    if (chatNodo != null) listadoChats.add(chatNodo)
                }
                listadoChats.sortBy { it.fecha }
                adapter.actualizarAdapter(listadoChats)
                binding.rvChats.scrollToPosition(listadoChats.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_error_recuperar_chats),
                    Toast.LENGTH_SHORT
                ).show()

                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_error_recuperar_chats_detalle, error.message),
                    Toast.LENGTH_LONG
                ).show()
                error.toException().printStackTrace()
            }
        })

        binding.etMensaje.setOnEditorActionListener { _, actionId, event ->
            val enviarConEnter =
                actionId == EditorInfo.IME_ACTION_DONE ||
                        (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (enviarConEnter) {
                enviar()
                ocultarTeclado()
                true
            } else false
        }
    }

    private fun enviar() {
        val texto = binding.etMensaje.text.toString().trim()
        if (texto.isEmpty()) return

        val fecha = System.currentTimeMillis()
        val mensaje = ChatModel(emailUsuarioLogeado, texto, fecha)
        val key = fecha.toString()

        databaseRef.child(key).setValue(mensaje)
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_error_guardar_mensaje),
                    Toast.LENGTH_SHORT
                ).show()
            }

        binding.etMensaje.setText("")
    }

    private fun ocultarTeclado() {
        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireActivity())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

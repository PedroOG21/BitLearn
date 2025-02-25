package com.example.proyectoandroid.fragments

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.databinding.FragmentChatBinding
import com.example.proyectoandroid.domain.models.ChatModel
import com.example.proyectoandroid.ui.adapter.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class Fragment_chat : Fragment() {

    private lateinit var binding: FragmentChatBinding

    var emailUsuarioLogeado = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private var listadoChats = mutableListOf<ChatModel>()
    private lateinit var adapter: ChatAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater)
        auth = Firebase.auth
        emailUsuarioLogeado = auth.currentUser?.email.toString()

        // No hace falta ya al tener la instancia antes

        // val firebaseDatabase = FirebaseDatabase.getInstance()
        // firebaseDatabase.setPersistenceEnabled(true)

        databaseRef = FirebaseDatabase.getInstance().getReference("chat")

        setRecycler()
        setListeners()
        return binding.root
    }

    private fun setRecycler() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvChats.layoutManager = layoutManager

        adapter = ChatAdapter(listadoChats, emailUsuarioLogeado) { mensaje ->
            eliminarMensaje(mensaje)
        }
        binding.rvChats.adapter = adapter
    }

    private fun eliminarMensaje(mensaje: ChatModel) {
        databaseRef.orderByChild("fecha").equalTo(mensaje.fecha.toDouble())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (nodo in snapshot.children) {
                        nodo.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Mensaje eliminado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Error al eliminar mensaje",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error de base de datos", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun setListeners() {
        binding.imageView.setOnClickListener {
            enviar()
        }
        //Ponemos un listener a la base de datos para recuperar todos los mensajes
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listadoChats.clear()
                for (nodo in snapshot.children) {
                    val chatNodo = nodo.getValue(ChatModel::class.java)
                    if (chatNodo != null) {
                        listadoChats.add(chatNodo)
                    }
                }
                listadoChats.sortBy { it.fecha }

                adapter.updateAdapter(listadoChats)

                //hacemos scroll de recycler para que aparezca su final
                binding.rvChats.scrollToPosition(listadoChats.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error al recuperar los chats", Toast.LENGTH_SHORT)
                    .show()

                Toast.makeText(
                    requireContext(),
                    "Error al recuperar los chats: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                error.toException().printStackTrace()
            }


        })
        //Listener para enviar chat cuando le de a intro
        binding.etMensaje.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                enviar()
                ocultarTeclado()
                true
            } else {
                false
            }
        }
    }

    private fun enviar() {
        val texto = binding.etMensaje.text.toString().trim()
        if (texto.isEmpty()) return
        val fecha = System.currentTimeMillis()
        val mensaje = ChatModel(emailUsuarioLogeado, texto, fecha)
        val key = fecha.toString()
        databaseRef.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                databaseRef.child(key).setValue(mensaje)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "No se pudo guardar el mensaje",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        binding.etMensaje.setText("")
    }

    private fun ocultarTeclado() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireActivity())
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
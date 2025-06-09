package com.example.proyectoandroid.data.database

import com.example.proyectoandroid.data.AmigosModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Task

class CrudAmigos {

    private val auth = Firebase.auth
    private val uid = auth.currentUser?.uid
        ?: throw IllegalStateException("Usuario no autenticado")
    private val dbRef = FirebaseDatabase.getInstance().reference
        .child("usuarios").child(uid).child("amigos")


    fun create(c: AmigosModel): Task<Void> {
        c.fechaAmistad = System.currentTimeMillis()
        return dbRef.child(c.id).setValue(c)
    }

    fun read(callback: (MutableList<AmigosModel>) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<AmigosModel>()
                snapshot.children.forEach { child ->
                    child.getValue(AmigosModel::class.java)?.let { modelo ->
                        lista.add(modelo)
                    }
                }
                callback(lista)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun actualizar(c: AmigosModel): Task<Void> {
        return dbRef.child(c.id).child("nombre").setValue(c.nombre)
    }

    fun borrar(id: String): Task<Void> {
        return dbRef.child(id).removeValue()
    }
}
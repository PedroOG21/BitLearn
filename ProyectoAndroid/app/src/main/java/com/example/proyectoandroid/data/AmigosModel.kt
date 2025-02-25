package com.example.proyectoandroid.data

import java.io.Serializable

data class AmigosModel(
    val id: Int,
    val nombre: String,
    val email: String,
    val imagen: String,
) : Serializable

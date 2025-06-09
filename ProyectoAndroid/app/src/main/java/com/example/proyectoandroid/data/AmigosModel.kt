package com.example.proyectoandroid.data

import java.io.Serializable

data class AmigosModel(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var imagen: String = "",
    var fechaAmistad: Long = 0L
) : Serializable


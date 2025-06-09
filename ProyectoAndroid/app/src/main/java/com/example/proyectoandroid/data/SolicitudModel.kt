package com.example.proyectoandroid.data

import java.io.Serializable

data class SolicitudModel(
    var id: String = "",
    var fromUid: String = "",
    var fromNombre: String = "",
    var fromEmail: String = "",
    var fromImagen: String = "",
    var timestamp: Long = 0L
) : Serializable


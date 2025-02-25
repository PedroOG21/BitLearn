package com.example.proyectoandroid.domain.models

import java.io.Serializable

data class ChatModel(
    val email: String = "",
    val mensaje: String = "",
    val fecha: Long = 0
) : Serializable
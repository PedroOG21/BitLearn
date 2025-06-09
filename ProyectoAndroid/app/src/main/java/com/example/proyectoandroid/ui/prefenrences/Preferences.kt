package com.example.proyectoandroid.ui.prefenrences

import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {
    private val storage: SharedPreferences =
        context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANG = "LANGUAGE"
        private const val KEY_NOMBRE = "NOM9BRE"
        private const val KEY_TELEFONO = "TELEFONO"
        private const val KEY_DIRECCION = "DIRECCION"
        private const val KEY_EDAD = "EDAD"
        private const val KEY_EMAIL = "EMAIL"
        private const val KEY_PERFIL_NOMBRE = "PERFIL_NOMBRE"
        private const val KEY_PERFIL_IMAGEN = "PERFIL_IMAGEN"
    }

    fun setDarkMode(isDarkMode: Boolean) {
        storage.edit().putBoolean("DARK_MODE", isDarkMode).apply()
    }

    fun isDarkMode(): Boolean =
        storage.getBoolean("DARK_MODE", true)

    fun saveUserData(
        nombre: String,
        telefono: String,
        direccion: String,
        edad: Int,
        email: String
    ) {
        storage.edit().apply {
            putString(KEY_NOMBRE, nombre)
            putString(KEY_TELEFONO, telefono)
            putString(KEY_DIRECCION, direccion)
            putInt(KEY_EDAD, edad)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun getNombre(): String =
        storage.getString(KEY_NOMBRE, "") ?: ""


    fun setIdioma(idioma: String) {
        storage.edit().putString(KEY_LANG, idioma).apply()
    }

    fun getIdioma(): String? =
        storage.getString(KEY_LANG, null)


    fun limpiarCambioIdioma() {
        storage.edit().putBoolean("idioma_cambiado", false).apply()
    }

    fun haCambiadoIdioma(): Boolean =
        storage.getBoolean("idioma_cambiado", false)


    fun getPerfilNombre(): String =
        storage.getString(KEY_PERFIL_NOMBRE, getNombre()) ?: getNombre()

    fun setPerfilImagen(imagen: String) {
        storage.edit().putString(KEY_PERFIL_IMAGEN, imagen).apply()
    }

    fun getPerfilImagen(): String =
        storage.getString(KEY_PERFIL_IMAGEN, "") ?: ""

}

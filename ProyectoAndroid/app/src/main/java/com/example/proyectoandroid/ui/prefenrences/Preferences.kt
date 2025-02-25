package com.example.proyectoandroid.ui.prefenrences


import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {
    private val storage: SharedPreferences =
        context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)


    fun setDarkMode(isDarkMode: Boolean) {
        storage.edit().putBoolean("DARK_MODE", isDarkMode).apply()
    }

    fun isDarkMode(): Boolean {
        return storage.getBoolean("DARK_MODE", true)
    }

    fun getPasosIniciales(): Float? {
        return if (storage.contains("PASOS_INICIALES"))
            storage.getFloat("PASOS_INICIALES", 0f)
        else null
    }

    fun setPasosIniciales(steps: Float) {
        storage.edit().putFloat("PASOS_INICIALES", steps).apply()
    }


    fun saveUserData(nombre: String, telefono: String, direccion: String, edad: Int, email: String) {
        storage.edit().apply {
            putString("NOMBRE", nombre)
            putString("TELEFONO", telefono)
            putString("DIRECCION", direccion)
            putInt("EDAD", edad)
            putString("EMAIL", email)
            apply()
        }
    }

    fun getNombre(): String = storage.getString("NOMBRE", "") ?: ""
    fun getTelefono(): String = storage.getString("TELEFONO", "") ?: ""
    fun getDireccion(): String = storage.getString("DIRECCION", "") ?: ""
    fun getEdad(): Int = storage.getInt("EDAD", 18)
}
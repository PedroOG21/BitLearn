package com.example.proyectoandroid.data.database

import android.app.Application
import android.content.Context

class Aplication : Application() {
    companion object {
        const val VERSION = 1
        const val DB = "Base_1"
        const val TABLA = "amigos"
        lateinit var appContext: Context
        lateinit var llave: MyDatabase
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        llave = MyDatabase()
    }
}
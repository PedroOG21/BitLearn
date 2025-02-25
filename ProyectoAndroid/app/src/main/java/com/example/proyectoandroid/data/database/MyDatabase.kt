package com.example.proyectoandroid.data.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabase() :
    SQLiteOpenHelper(Aplication.appContext, Aplication.DB, null, Aplication.VERSION) {

    private val q = "create table ${Aplication.TABLA}(" +
            "id integer primary key autoincrement," +
            "nombre text not null," +
            "email text not null unique," +
            "imagen text not null);"

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(q)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) {
            val borrarTabla = "drop table ${Aplication.TABLA};"
            db?.execSQL(borrarTabla)
            onCreate(db)
        }
    }

}
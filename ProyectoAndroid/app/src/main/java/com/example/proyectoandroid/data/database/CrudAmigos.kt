package com.example.proyectoandroid.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.proyectoandroid.data.AmigosModel

class CrudAmigos {

    fun create(c: AmigosModel): Long {
        val con = Aplication.llave.writableDatabase //abrimos la bbdd en modo escritura
        return try {
            con.insertWithOnConflict(
                Aplication.TABLA,
                null,
                c.toContentValues(),
                SQLiteDatabase.CONFLICT_IGNORE
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            -1L
        } finally {
            con.close()

        }
    }

    fun read(): MutableList<AmigosModel> {
        val lista = mutableListOf<AmigosModel>()
        val con = Aplication.llave.readableDatabase
        try {
            val cursor = con.query(
                Aplication.TABLA,
                arrayOf("id", "nombre", "email", "imagen"),
                null,
                null,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                val contacto = AmigosModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                )
                lista.add(contacto)
            }
            cursor.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            con.close()
        }
        return lista
    }

    public fun borrar(id: Int): Boolean {
        val con = Aplication.llave.writableDatabase
        val amigoBorrado = con.delete(Aplication.TABLA, "id=?", arrayOf(id.toString()))
        con.close()
        return amigoBorrado > 0
    }

    public fun update(c: AmigosModel): Boolean {
        val con = Aplication.llave.writableDatabase
        val values = c.toContentValues()
        var filasAfectadas = 0
        val q = "select id from ${Aplication.TABLA} where email=? AND id <> ?"
        val cursor = con.rawQuery(q, arrayOf(c.email, c.id.toString()))
        val existeEmail = cursor.moveToFirst()
        cursor.close()
        if (!existeEmail) {
            filasAfectadas = con.update(Aplication.TABLA, values, "id=?", arrayOf(c.id.toString()))
        }
        con.close()
        return filasAfectadas > 0
    }


    private fun AmigosModel.toContentValues(): ContentValues {
        return ContentValues().apply {
            put("nombre", nombre)
            put("email", email)
            put("imagen", imagen)
        }
    }

}
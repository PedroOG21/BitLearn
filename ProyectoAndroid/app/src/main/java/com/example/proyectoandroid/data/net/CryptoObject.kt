package com.example.proyectoandroid.data.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CryptoObject {
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.coinmarketcap.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
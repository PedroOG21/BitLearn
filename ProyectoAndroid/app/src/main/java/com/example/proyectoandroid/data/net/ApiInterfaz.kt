package com.example.proyectoandroid.data.net

import com.example.apicrypto.domain.models.MarketModel
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterfaz {
    @GET("data-api/v3/cryptocurrency/listing?start=1&limit=500")
    suspend fun getMarketData(): Response<MarketModel>
}
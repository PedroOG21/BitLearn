package com.example.proyectoandroid.domain.models

data class CryptoInventoryItem(
    val id: String,
    val amount: Double,

    var name: String = "",
    var symbol: String = "",
    var price: Double = 0.0,
    var percentChange: Double = 0.0,

    var totalValue: Double = 0.0
)
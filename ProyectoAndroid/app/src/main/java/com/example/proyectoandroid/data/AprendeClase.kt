package com.example.proyectoandroid.data

class AprendeClase {

    fun ramdom():AprendeModelo{
        val posicion = (0..9).random()
        return lista[posicion]
    }

    val lista = listOf<AprendeModelo>(
        AprendeModelo("Bitcoin", "La criptomoneda original y la más reconocida a nivel mundial."),
        AprendeModelo("Ethereum", "Plataforma líder para contratos inteligentes y dApps."),
        AprendeModelo("Binance Coin", "Criptomoneda del exchange Binance, utilizada en tarifas y transacciones."),
        AprendeModelo("Cardano", "Blockchain de tercera generación con enfoque en seguridad y escalabilidad."),
        AprendeModelo("Solana", "Red de alto rendimiento, conocida por sus rápidas transacciones y bajas comisiones."),
        AprendeModelo("Ripple", "Orientada a pagos transfronterizos y soluciones para instituciones financieras."),
        AprendeModelo("Polkadot", "Facilita la interoperabilidad entre diversas blockchains."),
        AprendeModelo("Dogecoin", "Iniciada como broma, pero con fuerte respaldo comunitario."),
        AprendeModelo("Litecoin", "Ofrece transacciones rápidas y de bajo costo, considerada la plata al oro de Bitcoin."),
        AprendeModelo("Chainlink", "Red descentralizada de oráculos que conecta smart contracts con datos del mundo real.")
    )
}

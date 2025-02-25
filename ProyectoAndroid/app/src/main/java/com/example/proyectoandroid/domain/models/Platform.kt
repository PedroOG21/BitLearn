package com.example.apicrypto.domain.models

import java.io.Serializable

data class Platform(
    val id: Int,
    val name: String,
    val slug: String,
    val symbol: String,
    val token_address: String
) : Serializable
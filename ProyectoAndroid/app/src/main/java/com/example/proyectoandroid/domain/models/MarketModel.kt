package com.example.apicrypto.domain.models

import java.io.Serializable

data class MarketModel(
    val `data`: Data,
    val status: Status
) : Serializable
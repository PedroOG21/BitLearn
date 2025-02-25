package com.example.apicrypto.domain.models

import java.io.Serializable

data class Data(
    val cryptoCurrencyList: List<CryptoCurrency>,
    val totalCount: String
) : Serializable
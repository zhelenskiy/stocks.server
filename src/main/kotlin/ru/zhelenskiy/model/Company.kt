package ru.zhelenskiy.model

import kotlinx.serialization.Serializable

@Serializable
data class Company(val name: String, val stocksState: StocksState)

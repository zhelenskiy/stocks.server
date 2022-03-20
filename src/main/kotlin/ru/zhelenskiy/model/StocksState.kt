package ru.zhelenskiy.model

import kotlinx.serialization.Serializable

@Serializable
data class StocksState(var count: Long, var price: Long) {
    init {
        require(count > 0)
        require(price > 0)
    }
}
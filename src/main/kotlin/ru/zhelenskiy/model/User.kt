package ru.zhelenskiy.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, var freeMoney: Long)

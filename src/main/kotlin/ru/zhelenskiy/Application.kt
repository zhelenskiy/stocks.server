package ru.zhelenskiy

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import ru.zhelenskiy.model.Company
import ru.zhelenskiy.model.StockMarket
import ru.zhelenskiy.model.StocksState
import ru.zhelenskiy.model.User
import ru.zhelenskiy.model.inmemory.InMemotyStockMarket
import ru.zhelenskiy.plugins.*

private fun StockMarket.addSampleData() = runBlocking {
    val yandex = addCompany(Company("Yandex", StocksState(100, 1000)))
    val google = addCompany(Company("Google", StocksState(200, 2000)))
    val evgeniy = addUser(User("Evgeniy", 10000))
    val petr = addUser(User("Petr", 8000))
    buyStocks(userId = evgeniy, companyId = yandex, count = 1, price = 1000)
    buyStocks(userId = evgeniy, companyId = google, count = 2, price = 2000)
    buyStocks(userId = petr, companyId = yandex, count = 2, price = 1000)
    buyStocks(userId = petr, companyId = google, count = 1, price = 2000)
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureService(InMemotyStockMarket().apply { addSampleData() })
    }.start(wait = true)
}

private fun Application.configureService(stockMarket: StockMarket) {
    configureSerialization()
    configureAdministration()
    configureRouting(stockMarket)
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

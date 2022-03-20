package ru.zhelenskiy.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import ru.zhelenskiy.model.Company
import ru.zhelenskiy.model.StockMarket
import ru.zhelenskiy.model.StocksState
import ru.zhelenskiy.model.User

typealias Handler<T> = suspend PipelineContext<*, ApplicationCall>.(T) -> Unit

suspend fun <T: Any> PipelineContext<*, ApplicationCall>.onParameter(
    name: String, validate: (String) -> T?, then: Handler<T>,
) = call.parameters[name]?.let(validate)?.let { then(it) }
    ?: call.respond(HttpStatusCode.BadRequest, "Bad $name")

suspend fun PipelineContext<*, ApplicationCall>.onString(name: String, then: Handler<String>) =
    onParameter(name, { it }, then)

suspend fun PipelineContext<*, ApplicationCall>.onInt(name: String, then: Handler<Int>) =
    onParameter(name, { it.toIntOrNull() }, then)

suspend fun PipelineContext<*, ApplicationCall>.onLong(name: String, then: Handler<Long>) =
    onParameter(name, { it.toLongOrNull() }, then)

fun Application.configureRouting(stockMarket: StockMarket) {
    routing {
        getMethods(stockMarket)
        postMethods(stockMarket)
        putMethods(stockMarket)
    }
}

private fun Routing.putMethods(stockMarket: StockMarket) {
    put("/changePrice/{companyId}/{newPrice}") {
        onInt("companyId") { companyId ->
            onLong("newPrice") { newPrice ->
                call.respond(stockMarket.changePrice(companyId, newPrice))
            }
        }
    }
}


private fun Routing.postMethods(stockMarket: StockMarket) {
    post("/addCompany/{name}") {
        onString("name") { name ->
            val count = call.request.queryParameters["count"]?.toLong()?.takeIf { it >= 0 } ?: 0
            val price = call.request.queryParameters["price"]?.toLong()?.takeIf { it >= 0 } ?: 0
            call.respond(stockMarket.addCompany(Company(name, StocksState(count, price))))
        }
    }
    post("/addStocks/{companyId}/{count}") {
        onInt("companyId") { companyId ->
            onLong("count") { count ->
                call.respond(stockMarket.addStocks(companyId, count))
            }
        }
    }
    post("/addFreeMoney/{userId}/{amount}") {
        onInt("userId") { userId ->
            onLong("amount") { amount ->
                call.respond(stockMarket.addFreeMoney(userId, amount))
            }
        }
    }
    post("/addUser/{name}") {
        onString("name") { name ->
            val freeMoney = call.request.queryParameters["freeMoney"]?.toLong()?.takeIf { it >= 0 } ?: 0
            call.respond(stockMarket.addUser(User(name, freeMoney)))
        }
    }
    post("/buyStocks/{userId}/{companyId}/{count}/{price}") {
        onInt("userId") { userId ->
            onInt("companyId") { companyId ->
                onLong("count") { count ->
                    onLong("price") { price ->
                        call.respond(stockMarket.buyStocks(userId, companyId, count, price))
                    }
                }
            }
        }
    }
    post("/sellStocks/{userId}/{companyId}/{count}/{price}") {
        onInt("userId") { userId ->
            onInt("companyId") { companyId ->
                onLong("count") { count ->
                    onLong("price") { price ->
                        call.respond(stockMarket.sellStocks(userId, companyId, count, price))
                    }
                }
            }
        }
    }
}

private fun Routing.getMethods(stockMarket: StockMarket) {
    get("/getCompanyById/{companyId}") {
        onInt("companyId") {
            call.respond(stockMarket.getCompanyById(it) ?: return@onInt call.respond(HttpStatusCode.NotFound))
        }
    }
    get("/getUserById/{userId}") {
        onInt("userId") {
            call.respond(stockMarket.getUserById(it) ?:  return@onInt call.respond(HttpStatusCode.NotFound))
        }
    }
    get("/getStocksCompaniesByUser/{userId}") {
        onInt("userId") {
            call.respond(stockMarket.getStocksCompaniesByUser(it))
        }
    }
    get("/getStockHoldersByCompany/{companyId}") {
        onInt("companyId") {
            call.respond(stockMarket.getStockHoldersByCompany(it))
        }
    }
    get("/getUserStocksCount/{companyId}/{userId}") {
        onInt("companyId") { companyId ->
            onInt("userId") { userId ->
                call.respond(stockMarket.getUserStocksCount(companyId, userId))
            }
        }
    }
}

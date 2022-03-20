package ru.zhelenskiy.model.inmemory

import ru.zhelenskiy.model.Company
import ru.zhelenskiy.model.StockMarket
import ru.zhelenskiy.model.User
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class InMemotyStockMarket : StockMarket {
    private val lock = ReentrantReadWriteLock()

    private val companies = mutableListOf<Company>()
    private val users = mutableListOf<User>()
    private val stockCompaniesByUser: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
    private val stockHoldersByCompany: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
    private val userStocksCount: MutableMap<Pair<Int, Int>, Long> = mutableMapOf()

    override suspend fun getCompanyById(companyId: Int): Company? = lock.read { companies.getOrNull(companyId) }

    override suspend fun getUserById(userId: Int): User? = lock.read { users.getOrNull(userId) }

    override suspend fun getStocksCompaniesByUser(userId: Int): Set<Int> = lock.read {
        stockCompaniesByUser[userId] ?: setOf()
    }

    override suspend fun getStockHoldersByCompany(companyId: Int): Set<Int> = lock.read {
        stockHoldersByCompany[companyId] ?: setOf()
    }

    override suspend fun getUserStocksCount(companyId: Int, userId: Int): Long = lock.read {
        userStocksCount[companyId to userId] ?: 0L
    }

    override suspend fun addCompany(company: Company): Int = lock.write {
        val id = companies.size
        companies.add(company)
        id
    }

    override suspend fun addStocks(companyId: Int, count: Long): Boolean = if (count <= 0) false else lock.write {
        companies.getOrNull(companyId)?.stocksState?.let { it.count += count }
    } != null

    override suspend fun addUser(user: User): Int = lock.write {
        val id = users.size
        users.add(user)
        id
    }

    override suspend fun addFreeMoney(userId: Int, amount: Long): Boolean = if (amount <= 0) false else lock.write  {
        users.getOrNull(userId)?.let { it.freeMoney += amount }
    } != null

    override suspend fun buyStocks(userId: Int, companyId: Int, count: Long, price: Long): Boolean = lock.write {
        if (count <= 0 || price <= 0 || count > Long.MAX_VALUE / price) {
            return@write false
        }
        val user = getUserById(userId) ?: return@write false
        val company = getCompanyById(companyId) ?: return@write false
        val totalPrice = count * price
        return@write when {
            company.stocksState.price != price -> false
            company.stocksState.count < count -> false
            user.freeMoney < totalPrice -> false
            else -> {
                user.freeMoney -= totalPrice
                stockCompaniesByUser.getOrPut(userId) { mutableSetOf() }.add(companyId)
                stockHoldersByCompany.getOrPut(companyId) { mutableSetOf() }.add(userId)
                userStocksCount[companyId to userId] = getUserStocksCount(companyId, userId) + count
                company.stocksState.count -= count
                true
            }
        }

    }

    override suspend fun sellStocks(userId: Int, companyId: Int, count: Long, price: Long): Boolean = lock.write {
        if (count <= 0 || price <= 0 || count > Long.MAX_VALUE / price) {
            return@write false
        }
        val user = getUserById(userId) ?: return@write false
        val company = getCompanyById(companyId) ?: return@write false
        val totalPrice = count * price
        val oldUserStocksCount = getUserStocksCount(companyId, userId)
        return@write when {
            company.stocksState.price != price -> false
            oldUserStocksCount < count -> false
            else -> {
                user.freeMoney += totalPrice
                company.stocksState.count += count
                if (oldUserStocksCount > count) {
                    userStocksCount[companyId to userId] = oldUserStocksCount - count
                } else {
                    userStocksCount.remove(companyId to userId)
                    stockCompaniesByUser[userId]!!.apply {
                        remove(companyId)
                        if (isEmpty()) {
                            stockCompaniesByUser.remove(userId)
                        }
                    }
                    stockHoldersByCompany[companyId]!!.apply {
                        remove(userId)
                        if (isEmpty()) {
                            stockHoldersByCompany.remove(companyId)
                        }
                    }
                }
                true
            }
        }
    }

    override suspend fun changePrice(companyId: Int, newPrice: Long): Boolean = lock.write {
        if (newPrice <= 0) {
            return@write false
        }
        val company = getCompanyById(companyId) ?: return@write false
        company.stocksState.price = newPrice
        true
    }
}
package ru.zhelenskiy.model

interface StockMarket {
    suspend fun getCompanyById(companyId: Int): Company?
    suspend fun getUserById(userId: Int): User?
    suspend fun getStocksCompaniesByUser(userId: Int): Set<Int>
    suspend fun getStockHoldersByCompany(companyId: Int): Set<Int>
    suspend fun getUserStocksCount(companyId: Int, userId: Int): Long

    suspend fun addCompany(company: Company): Int
    suspend fun addStocks(companyId: Int, count: Long): Boolean
    suspend fun addUser(user: User): Int
    suspend fun addFreeMoney(userId: Int, amount: Long): Boolean
    suspend fun buyStocks(userId: Int, companyId: Int, count: Long, price: Long): Boolean
    suspend fun sellStocks(userId: Int, companyId: Int, count: Long, price: Long): Boolean
    suspend fun changePrice(companyId: Int, newPrice: Long): Boolean
}


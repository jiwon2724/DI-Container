package jiwondev.di_container.repository

import jiwondev.di_container.datasource.UserLocalDataSource

class UserDataRepository(private val localDataSource: UserLocalDataSource) {
    suspend fun login(id: String): Boolean {
        if (isLoggedIn()) return true
        if ("jiwon2724" != id) return false
        localDataSource.setLoginState(true)
        return true
    }
    suspend fun isLoggedIn(): Boolean = localDataSource.getLoginState()
    suspend fun logout(): Unit = localDataSource.clear()
}
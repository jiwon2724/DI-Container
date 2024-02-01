package jiwondev.di_container.repository

import jiwondev.di_container.datasource.UserLocalDataSource

class UserDataRepository(private val localDataSource: UserLocalDataSource) {
    suspend fun login(id: String): Boolean {
        if (isLoggedIn()) return true
        return id == "jiwon2724"
    }
    suspend fun isLoggedIn(): Boolean = localDataSource.getLoginState()
    suspend fun logout(): Unit = localDataSource.clear()
}
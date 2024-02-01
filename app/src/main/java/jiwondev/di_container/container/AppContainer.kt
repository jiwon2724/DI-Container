package jiwondev.di_container.container

import android.content.Context
import jiwondev.di_container.datasource.UserLocalDataSource

class AppContainer(private val context: Context) {
    fun createLocalDataSource(): UserLocalDataSource = UserLocalDataSource(context)
    var loginContainer: LoginContainer? = null
}
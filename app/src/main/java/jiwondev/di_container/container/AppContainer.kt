package jiwondev.di_container.container

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import jiwondev.di_container.datasource.UserLocalDataSource
import jiwondev.di_container.repository.UserDataRepository
import jiwondev.di_container.viewmodel.LoginViewModel

class AppContainer(private val context: Context) {
    fun createLocalDataSource(): UserLocalDataSource = UserLocalDataSource(context)

    fun createLocalDataRepository(): UserDataRepository = UserDataRepository(createLocalDataSource())

    fun createLoginViewModelFactory(): AbstractSavedStateViewModelFactory {
        return object : AbstractSavedStateViewModelFactory() {
            val localDataRepository = createLocalDataRepository()
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                return LoginViewModel(localDataRepository) as T
            }
        }
    }
}
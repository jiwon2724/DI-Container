package jiwondev.di_container.container

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import jiwondev.di_container.repository.UserDataRepository
import jiwondev.di_container.viewmodel.LoginViewModel

class LoginContainer(private val appContainer: AppContainer) {
    fun createLocalDataRepository(): UserDataRepository = UserDataRepository(appContainer.createLocalDataSource())

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
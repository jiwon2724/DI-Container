package jiwondev.di_container.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jiwondev.di_container.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserDataRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState(id = "jiwon2724"))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.isLoggedIn()) {
                _uiState.update { it.copy(userState = UserState.LOGGED_IN) }
            }
        }
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            val isLoggedIn: Boolean = repository.login(_uiState.value.id)
            _uiState.update {
                it.copy(userState = if (isLoggedIn) UserState.LOGGED_IN else UserState.FAILED)
            }
        }
    }
}

data class LoginUiState(
    val id: String,
    val userState: UserState = UserState.NONE
)

enum class UserState {
    NONE,
    FAILED,
    LOGGED_IN
}
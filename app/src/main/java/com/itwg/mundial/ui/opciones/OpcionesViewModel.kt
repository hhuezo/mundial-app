package com.itwg.mundial.ui.opciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.itwg.mundial.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpcionesPasswordUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

class OpcionesViewModel(
    private val userId: Long,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _passwordState = MutableStateFlow(OpcionesPasswordUiState())
    val passwordState: StateFlow<OpcionesPasswordUiState> = _passwordState.asStateFlow()

    fun resetPassword(password: String, confirmPassword: String) {
        when {
            password.length < 8 -> {
                _passwordState.update {
                    it.copy(
                        errorMessage = "La contraseña debe tener al menos 8 caracteres.",
                        successMessage = null,
                    )
                }
                return
            }
            password != confirmPassword -> {
                _passwordState.update {
                    it.copy(
                        errorMessage = "Las contraseñas no coinciden.",
                        successMessage = null,
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _passwordState.update {
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }
            authRepository.resetPassword(userId, password).fold(
                onSuccess = { message ->
                    _passwordState.update {
                        it.copy(isLoading = false, successMessage = message)
                    }
                },
                onFailure = { error ->
                    _passwordState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    fun clearPasswordMessages() {
        _passwordState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

class OpcionesViewModelFactory(
    private val userId: Long,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OpcionesViewModel::class.java)) {
            return OpcionesViewModel(userId, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

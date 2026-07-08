package com.duo.nebula.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duo.nebula.data.repository.AuthRepository
import com.duo.nebula.util.NebulaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) = _uiState.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "A senha precisa ter pelo menos 6 caracteres.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Cria a conta no Firebase Authentication e, em seguida, o documento
            // de perfil correspondente no Firestore (critério "Perfil do Usuário").
            when (val result = authRepository.register(state.name, state.email, state.password)) {
                is NebulaResult.Success -> _uiState.update {
                    it.copy(isLoading = false, registrationSucceeded = true)
                }
                is NebulaResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}

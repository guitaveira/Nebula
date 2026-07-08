package com.duo.nebula.ui.screens.register

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSucceeded: Boolean = false
)

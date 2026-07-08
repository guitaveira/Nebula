package com.duo.nebula.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.duo.nebula.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn
}

package com.duo.nebula.ui.screens.createpost

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duo.nebula.data.repository.AuthRepository
import com.duo.nebula.data.repository.PublicationRepository
import com.duo.nebula.util.ImageCompressor
import com.duo.nebula.util.NebulaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreatePublicationViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val publicationRepository: PublicationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePublicationUiState())
    val uiState: StateFlow<CreatePublicationUiState> = _uiState.asStateFlow()

    fun onImagePicked(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri, errorMessage = null) }
    }

    fun onCaptionChanged(value: String) {
        _uiState.update { it.copy(caption = value, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        val imageUri = state.imageUri
        val authorId = authRepository.currentUserId

        if (imageUri == null) {
            _uiState.update { it.copy(errorMessage = "Escolha uma imagem para publicar.") }
            return
        }
        if (authorId == null) {
            _uiState.update { it.copy(errorMessage = "Sessão expirada. Entre novamente.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                ImageCompressor.compress(appContext, imageUri)
            }
            if (bytes == null) {
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = "Não foi possível processar a imagem.")
                }
                return@launch
            }

            when (val result = publicationRepository.createPublication(authorId, state.caption, bytes)) {
                is NebulaResult.Success -> _uiState.update {
                    CreatePublicationUiState(publicationCreated = true)
                }
                is NebulaResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}

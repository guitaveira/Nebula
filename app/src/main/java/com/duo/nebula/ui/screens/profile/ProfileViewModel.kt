package com.duo.nebula.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duo.nebula.data.repository.AuthRepository
import com.duo.nebula.data.repository.ProfileRepository
import com.duo.nebula.data.repository.PublicationRepository
import com.duo.nebula.util.ImageCompressor
import com.duo.nebula.util.NebulaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val profileRepository: ProfileRepository,
    private val publicationRepository: PublicationRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** uid da aba "Perfil" (o próprio usuário) ou o uid recebido ao tocar em um autor no feed. */
    private val viewedUid: String =
        savedStateHandle.get<String>("uid") ?: authRepository.currentUserId.orEmpty()

    private val isOwnProfile: Boolean = viewedUid == authRepository.currentUserId

    private val _uiState = MutableStateFlow(ProfileUiState(isOwnProfile = isOwnProfile))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observePosts()
        observeFollowState()
    }

    private fun loadProfile() {
        if (viewedUid.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = profileRepository.getProfile(viewedUid)) {
                is NebulaResult.Success -> _uiState.update {
                    it.copy(profile = result.data, isLoading = false, errorMessage = null)
                }
                is NebulaResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun observePosts() {
        if (viewedUid.isBlank()) return
        publicationRepository.observeUserPublications(viewedUid, authRepository.currentUserId)
            .onEach { posts -> _uiState.update { it.copy(posts = posts) } }
            .catch { /* a grade de posts é secundária: falha aqui não deve travar a tela de perfil */ }
            .launchIn(viewModelScope)
    }

    fun onPhotoPicked(uri: Uri?) {
        if (!isOwnProfile || uri == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val bytes = withContext(Dispatchers.IO) { ImageCompressor.compress(appContext, uri) }
            if (bytes == null) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Não foi possível processar a imagem.") }
                return@launch
            }
            when (val result = profileRepository.updatePhoto(viewedUid, bytes)) {
                is NebulaResult.Success -> {
                    val current = _uiState.value.profile
                    _uiState.update { it.copy(isSaving = false, profile = current?.copy(photoUrl = result.data)) }
                    publicationRepository.invalidateAuthorCache(viewedUid)
                }
                is NebulaResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    fun startEditingBio() {
        if (!isOwnProfile) return
        val current = _uiState.value.profile ?: return
        _uiState.update { it.copy(isEditingBio = true, bioDraft = current.bio) }
    }

    fun onBioDraftChanged(newBio: String) {
        if (!isOwnProfile) return
        _uiState.update { it.copy(bioDraft = newBio) }
    }

    fun cancelEditingBio() {
        _uiState.update { it.copy(isEditingBio = false) }
    }

    fun saveBio() {
        if (!isOwnProfile) return
        val newBio = _uiState.value.bioDraft
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = profileRepository.updateBio(viewedUid, newBio)) {
                is NebulaResult.Success -> {
                    val current = _uiState.value.profile
                    _uiState.update {
                        it.copy(
                            isEditingBio = false,
                            isSaving = false,
                            profile = current?.copy(bio = newBio.trim())
                        )
                    }
                }
                is NebulaResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun startEditingName() {
        if (!isOwnProfile) return
        val current = _uiState.value.profile ?: return
        _uiState.update { it.copy(isEditingName = true, nameDraft = current.displayName) }
    }

    fun onNameDraftChanged(value: String) {
        _uiState.update { it.copy(nameDraft = value) }
    }

    fun cancelEditingName() {
        _uiState.update { it.copy(isEditingName = false) }
    }

    fun saveName() {
        if (!isOwnProfile) return
        val newName = _uiState.value.nameDraft
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = profileRepository.updateDisplayName(viewedUid, newName)) {
                is NebulaResult.Success -> {
                    val current = _uiState.value.profile
                    _uiState.update {
                        it.copy(
                            isEditingName = false,
                            isSaving = false,
                            profile = current?.copy(displayName = newName.trim()),
                            errorMessage = null
                        )
                    }
                    publicationRepository.invalidateAuthorCache(viewedUid)
                }
                is NebulaResult.Error -> _uiState.update {
                    it.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    fun logout() {
        if (!isOwnProfile) return
        authRepository.logout()
        _uiState.update { it.copy(loggedOut = true) }
    }

    /** Observa em tempo real se o usuário logado segue o perfil visualizado. */
    private fun observeFollowState() {
        if (isOwnProfile) return
        val myUid = authRepository.currentUserId ?: return
        profileRepository.observeIsFollowing(myUid, viewedUid)
            .onEach { following -> _uiState.update { it.copy(isFollowing = following) } }
            .catch { /* estado secundário: uma falha aqui não deve travar a tela de perfil */ }
            .launchIn(viewModelScope)
    }

    /** Segue ou deixa de seguir o perfil visualizado, com atualização otimista da UI. */
    fun toggleFollow() {
        if (isOwnProfile) return
        val myUid = authRepository.currentUserId ?: return
        val current = _uiState.value
        if (current.isTogglingFollow) return
        val nowFollowing = !current.isFollowing
        val currentProfile = current.profile

        _uiState.update {
            it.copy(
                isFollowing = nowFollowing,
                isTogglingFollow = true,
                profile = currentProfile?.copy(
                    followersCount = currentProfile.followersCount + if (nowFollowing) 1 else -1
                )
            )
        }

        viewModelScope.launch {
            val result = profileRepository.toggleFollow(myUid, viewedUid, nowFollowing)
            if (result is NebulaResult.Error) {
                // Reverte a atualização otimista em caso de falha.
                _uiState.update {
                    val revertProfile = it.profile
                    it.copy(
                        isFollowing = !nowFollowing,
                        isTogglingFollow = false,
                        errorMessage = result.message,
                        profile = revertProfile?.copy(
                            followersCount = revertProfile.followersCount + if (nowFollowing) -1 else 1
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(isTogglingFollow = false) }
            }
        }
    }
}

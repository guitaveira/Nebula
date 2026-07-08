package com.duo.nebula.ui.screens.profile

import com.duo.nebula.data.model.PublicationFeedItem
import com.duo.nebula.data.model.UserProfile

data class ProfileUiState(
    val profile: UserProfile? = null,
    val posts: List<PublicationFeedItem> = emptyList(),
    val isOwnProfile: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val loggedOut: Boolean = false,
    // Edição do nome de usuário: enquanto "isEditingName" for true, o campo
    // de texto exibe "nameDraft" em vez do nome já salvo no perfil.
    val isEditingName: Boolean = false,
    val nameDraft: String = "",
    // Edição da bio: enquanto "isEditingBio" for true, o campo
    // exibe "bioDraft" e mostra botões de salvar/cancelar.
    val isEditingBio: Boolean = false,
    val bioDraft: String = "",
    // Estado de "seguir": só relevante quando isOwnProfile == false.
    val isFollowing: Boolean = false,
    val isTogglingFollow: Boolean = false
)

package com.duo.nebula.ui.screens.createpost

import android.net.Uri

data class CreatePublicationUiState(
    val imageUri: Uri? = null,
    val caption: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val publicationCreated: Boolean = false
)

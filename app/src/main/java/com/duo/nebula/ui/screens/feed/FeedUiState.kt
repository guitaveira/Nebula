package com.duo.nebula.ui.screens.feed

import com.duo.nebula.data.model.CommentUiModel
import com.duo.nebula.data.model.PublicationFeedItem

data class FeedUiState(
    val items: List<PublicationFeedItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Estado da folha (bottom sheet) de comentários aberta para uma publicação.
    val openCommentsForPublicationId: String? = null,
    val comments: List<CommentUiModel> = emptyList(),
    val commentDraft: String = "",
    val isSendingComment: Boolean = false
)

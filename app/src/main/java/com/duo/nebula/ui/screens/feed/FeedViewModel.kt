package com.duo.nebula.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duo.nebula.data.repository.AuthRepository
import com.duo.nebula.data.repository.PublicationRepository
import com.duo.nebula.util.NebulaResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var commentsJob: Job? = null
    private val currentUserId: String? get() = authRepository.currentUserId

    init {
        observeFeed()
    }

    private fun observeFeed() {
        publicationRepository.observeFeed(currentUserId)
            .onEach { items -> _uiState.update { it.copy(items = items, isLoading = false, errorMessage = null) } }
            .catch { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = throwable.message ?: "Não foi possível carregar o feed.")
                }
            }
            .launchIn(viewModelScope)
    }

    /** Curtir/descurtir uma publicação. Atualiza a UI de forma otimista. */
    fun toggleLike(publicationId: String) {
        val uid = currentUserId ?: return
        val current = _uiState.value.items.firstOrNull { it.publication.id == publicationId } ?: return
        val nowLiked = !current.isLikedByMe

        _uiState.update { state ->
            state.copy(items = state.items.map { item ->
                if (item.publication.id != publicationId) return@map item
                val updatedLikedBy = if (nowLiked) {
                    item.publication.likedBy + uid
                } else {
                    item.publication.likedBy - uid
                }
                item.copy(publication = item.publication.copy(likedBy = updatedLikedBy), isLikedByMe = nowLiked)
            })
        }

        viewModelScope.launch {
            val result = publicationRepository.toggleLike(publicationId, uid, nowLiked)
            if (result is NebulaResult.Error) {
                // Reverte em caso de falha, já que o listener em tempo real vai corrigir de qualquer forma.
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    /** Abre a folha de comentários de uma publicação e começa a observá-los em tempo real. */
    fun openComments(publicationId: String) {
        commentsJob?.cancel()
        _uiState.update { it.copy(openCommentsForPublicationId = publicationId, comments = emptyList(), commentDraft = "") }
        commentsJob = publicationRepository.observeComments(publicationId, currentUserId)
            .onEach { comments -> _uiState.update { it.copy(comments = comments) } }
            .launchIn(viewModelScope)
    }

    fun closeComments() {
        commentsJob?.cancel()
        _uiState.update { it.copy(openCommentsForPublicationId = null, comments = emptyList(), commentDraft = "") }
    }

    fun onCommentDraftChanged(text: String) {
        _uiState.update { it.copy(commentDraft = text) }
    }

    fun submitComment() {
        val publicationId = _uiState.value.openCommentsForPublicationId ?: return
        val uid = currentUserId ?: return
        val text = _uiState.value.commentDraft
        if (text.isBlank()) return

        _uiState.update { it.copy(isSendingComment = true) }
        viewModelScope.launch {
            when (val result = publicationRepository.addComment(publicationId, uid, text)) {
                is NebulaResult.Success -> _uiState.update { it.copy(isSendingComment = false, commentDraft = "") }
                is NebulaResult.Error -> _uiState.update { it.copy(isSendingComment = false, errorMessage = result.message) }
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        val publicationId = _uiState.value.openCommentsForPublicationId ?: return
        val uid = currentUserId ?: return
        val current = _uiState.value.comments.firstOrNull { it.comment.id == commentId } ?: return
        val nowLiked = !current.isLikedByMe

        _uiState.update { state ->
            state.copy(comments = state.comments.map { model ->
                if (model.comment.id != commentId) return@map model
                val updatedLikedBy = if (nowLiked) model.comment.likedBy + uid else model.comment.likedBy - uid
                model.copy(comment = model.comment.copy(likedBy = updatedLikedBy), isLikedByMe = nowLiked)
            })
        }

        viewModelScope.launch {
            publicationRepository.toggleCommentLike(publicationId, commentId, uid, nowLiked)
        }
    }
}

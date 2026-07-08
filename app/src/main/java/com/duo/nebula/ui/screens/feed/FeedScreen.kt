package com.duo.nebula.ui.screens.feed

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.duo.nebula.data.model.CommentUiModel
import com.duo.nebula.data.model.PublicationFeedItem
import com.duo.nebula.util.formatRelativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onAuthorClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
            state.errorMessage != null && state.items.isEmpty() -> Text(
                text = state.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
            state.items.isEmpty() -> Text(
                text = "Ainda não há publicações. Seja o primeiro a compartilhar!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = "Nébula",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(state.items, key = { it.publication.id }) { item ->
                    PublicationCard(
                        item = item,
                        onLikeClick = { viewModel.toggleLike(item.publication.id) },
                        onCommentClick = { viewModel.openComments(item.publication.id) },
                        onAuthorClick = { onAuthorClick(item.publication.authorId) }
                    )
                }
            }
        }

        if (state.openCommentsForPublicationId != null) {
            CommentsSheet(
                comments = state.comments,
                draft = state.commentDraft,
                isSending = state.isSendingComment,
                onDraftChanged = viewModel::onCommentDraftChanged,
                onSend = viewModel::submitComment,
                onLikeComment = viewModel::toggleCommentLike,
                onDismiss = viewModel::closeComments
            )
        }
    }
}

@Composable
private fun PublicationCard(
    item: PublicationFeedItem,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAuthorClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAuthorClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthorAvatar(photoUrl = item.authorPhotoUrl, contentDescription = item.authorName, size = 40.dp)
            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                Text(
                    text = item.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatRelativeTime(item.publication.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AsyncImage(
            model = item.publication.imageUrl,
            contentDescription = item.publication.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (item.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir",
                    tint = if (item.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${item.likesCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onCommentClick) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = "Comentar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${item.commentsCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (item.publication.caption.isNotBlank()) {
            Text(
                text = item.publication.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AuthorAvatar(photoUrl: String, contentDescription: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (photoUrl.isNotBlank()) {
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.PersonOutline,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.55f),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsSheet(
    comments: List<CommentUiModel>,
    draft: String,
    isSending: Boolean,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onLikeComment: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .imePadding()
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "Comentários",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            AnimatedContent(targetState = comments.isEmpty(), modifier = Modifier.weight(1f)) { isEmpty ->
                if (isEmpty) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Seja o primeiro a comentar.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(comments, key = { it.comment.id }) { commentModel ->
                            CommentRow(commentModel, onLikeClick = { onLikeComment(commentModel.comment.id) })
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChanged,
                    placeholder = { Text("Adicione um comentário...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (draft.isNotBlank() && !isSending) onSend() }),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSend, enabled = !isSending && draft.isNotBlank()) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Enviar comentário",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(model: CommentUiModel, onLikeClick: () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        AuthorAvatar(photoUrl = model.comment.authorPhotoUrl, contentDescription = model.comment.authorName, size = 32.dp)
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = model.comment.authorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatRelativeTime(model.comment.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = model.comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = if (model.isLikedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Curtir comentário",
                    tint = if (model.isLikedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (model.likesCount > 0) {
                Text(
                    text = "${model.likesCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

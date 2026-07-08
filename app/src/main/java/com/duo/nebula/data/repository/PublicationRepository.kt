package com.duo.nebula.data.repository

import com.duo.nebula.data.model.Comment
import com.duo.nebula.data.model.CommentUiModel
import com.duo.nebula.data.model.Publication
import com.duo.nebula.data.model.PublicationFeedItem
import com.duo.nebula.data.remote.ProfileRemoteSource
import com.duo.nebula.data.remote.PublicationRemoteSource
import com.duo.nebula.util.NebulaResult
import com.duo.nebula.util.mapFirebaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicationRepository @Inject constructor(
    private val publicationRemoteSource: PublicationRemoteSource,
    private val profileRemoteSource: ProfileRemoteSource
) {

    // Cache simples em memória para evitar buscar o mesmo perfil de autor
    // repetidamente enquanto o feed recebe atualizações em tempo real.
    private val authorCache = mutableMapOf<String, Pair<String, String>>()

    /** Feed em tempo real: cada novo snapshot do Firestore já vem com autor resolvido. */
    fun observeFeed(currentUserId: String?): Flow<List<PublicationFeedItem>> {
        return publicationRemoteSource.observePublications().map { publications ->
            publications.map { publication -> attachAuthor(publication, currentUserId) }
        }
    }

    /** Publicações de um autor específico, usadas na grade do perfil. */
    fun observeUserPublications(authorId: String, currentUserId: String?): Flow<List<PublicationFeedItem>> {
        return publicationRemoteSource.observePublicationsByAuthor(authorId).map { publications ->
            publications.map { publication -> attachAuthor(publication, currentUserId) }
        }
    }

    private suspend fun attachAuthor(publication: Publication, currentUserId: String?): PublicationFeedItem {
        val cached = authorCache[publication.authorId]
        val (name, photoUrl) = cached ?: run {
            val profile = profileRemoteSource.fetchProfile(publication.authorId)
            val resolved = (profile?.displayName ?: "Usuário") to (profile?.photoUrl ?: "")
            authorCache[publication.authorId] = resolved
            resolved
        }
        val isLiked = currentUserId != null && publication.likedBy.contains(currentUserId)
        return PublicationFeedItem(publication, name, photoUrl, isLiked)
    }

    /** Faz upload da imagem e persiste a nova publicação no Firestore. */
    suspend fun createPublication(
        authorId: String,
        caption: String,
        imageBytes: ByteArray
    ): NebulaResult<Unit> {
        return try {
            val imageUrl = publicationRemoteSource.uploadPublicationImage(authorId, imageBytes)
            val publication = Publication(
                authorId = authorId,
                caption = caption.trim(),
                imageUrl = imageUrl
            )
            publicationRemoteSource.createPublication(publication)
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    /** Alterna a curtida do usuário atual em uma publicação. */
    suspend fun toggleLike(publicationId: String, uid: String, liked: Boolean): NebulaResult<Unit> {
        return try {
            publicationRemoteSource.setLiked(publicationId, uid, liked)
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    /** Observa os comentários de uma publicação já com o estado de curtida do usuário atual. */
    fun observeComments(publicationId: String, currentUserId: String?): Flow<List<CommentUiModel>> {
        return publicationRemoteSource.observeComments(publicationId).map { comments ->
            comments.map { comment ->
                val isLiked = currentUserId != null && comment.likedBy.contains(currentUserId)
                CommentUiModel(comment, isLiked)
            }
        }
    }

    /** Cria um novo comentário, resolvendo o nome/foto do autor a partir do cache/perfil. */
    suspend fun addComment(publicationId: String, authorId: String, text: String): NebulaResult<Unit> {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return NebulaResult.Error("Escreva algo para comentar.")
        return try {
            val cached = authorCache[authorId]
            val (name, photoUrl) = cached ?: run {
                val profile = profileRemoteSource.fetchProfile(authorId)
                val resolved = (profile?.displayName ?: "Usuário") to (profile?.photoUrl ?: "")
                authorCache[authorId] = resolved
                resolved
            }
            val comment = Comment(
                authorId = authorId,
                authorName = name,
                authorPhotoUrl = photoUrl,
                text = trimmed
            )
            publicationRemoteSource.addComment(publicationId, comment)
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    /** Alterna a curtida do usuário atual em um comentário. */
    suspend fun toggleCommentLike(
        publicationId: String,
        commentId: String,
        uid: String,
        liked: Boolean
    ): NebulaResult<Unit> {
        return try {
            publicationRemoteSource.setCommentLiked(publicationId, commentId, uid, liked)
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    /** Invalida o cache de um autor (usado após editar nome/foto do próprio perfil). */
    fun invalidateAuthorCache(uid: String) {
        authorCache.remove(uid)
    }
}

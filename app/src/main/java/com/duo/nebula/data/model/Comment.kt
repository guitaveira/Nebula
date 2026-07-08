package com.duo.nebula.data.model

/**
 * Representa um comentário na subcoleção "comments" de uma publicação.
 * Nome e foto do autor são desnormalizados no momento da criação para
 * evitar uma consulta extra de perfil ao renderizar a lista de comentários.
 */
data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "authorId" to authorId,
        "authorName" to authorName,
        "authorPhotoUrl" to authorPhotoUrl,
        "text" to text,
        "createdAt" to createdAt,
        "likedBy" to likedBy
    )
}

/** Modelo de UI: comentário + se o usuário atual já curtiu. */
data class CommentUiModel(
    val comment: Comment,
    val isLikedByMe: Boolean = false
) {
    val likesCount: Int get() = comment.likedBy.size
}

package com.duo.nebula.data.model

/**
 * Representa uma publicação (post) na coleção "publications" do Firestore.
 * "likedBy" guarda os uids de quem curtiu (abordagem simples com arrayUnion/
 * arrayRemove, sem necessidade de subcoleção só para contar curtidas).
 * "commentsCount" é incrementado de forma atômica a cada novo comentário.
 */
data class Publication(
    val id: String = "",
    val authorId: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList(),
    val commentsCount: Long = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "authorId" to authorId,
        "caption" to caption,
        "imageUrl" to imageUrl,
        "createdAt" to createdAt,
        "likedBy" to likedBy,
        "commentsCount" to commentsCount
    )
}

/**
 * Modelo combinado usado apenas na camada de UI: junta a publicação com os
 * dados do autor (nome e foto) já resolvidos, e o estado de curtida do
 * usuário atual, para simplificar a renderização do feed.
 */
data class PublicationFeedItem(
    val publication: Publication,
    val authorName: String,
    val authorPhotoUrl: String,
    val isLikedByMe: Boolean = false
) {
    val likesCount: Int get() = publication.likedBy.size
    val commentsCount: Int get() = publication.commentsCount.toInt()
}

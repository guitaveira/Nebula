package com.duo.nebula.data.remote

import com.duo.nebula.data.model.Comment
import com.duo.nebula.data.model.Publication
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val PUBLICATIONS_COLLECTION = "publications"
private const val COMMENTS_SUBCOLLECTION = "comments"

@Singleton
class PublicationRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    /**
     * Observa a coleção de publicações em tempo real, ordenada da mais
     * recente para a mais antiga. Cada emissão do Flow reflete o estado
     * atual do banco, incluindo novas publicações, curtidas e contagem
     * de comentários atualizadas por qualquer usuário.
     */
    fun observePublications(): Flow<List<Publication>> = callbackFlow {
        val registration = firestore.collection(PUBLICATIONS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val publications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Publication::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(publications)
            }
        awaitClose { registration.remove() }
    }

    /** Observa em tempo real apenas as publicações de um autor específico (usado no perfil). */
    fun observePublicationsByAuthor(authorId: String): Flow<List<Publication>> = callbackFlow {
        val registration = firestore.collection(PUBLICATIONS_COLLECTION)
            .whereEqualTo("authorId", authorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val publications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Publication::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(publications)
            }
        awaitClose { registration.remove() }
    }

    /** Faz upload da imagem da publicação e retorna a URL pública gerada. */
    suspend fun uploadPublicationImage(authorId: String, bytes: ByteArray): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("publication_photos/$authorId/$fileName")
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    /** Persiste a publicação (com a URL da imagem já enviada) no Firestore. */
    suspend fun createPublication(publication: Publication) {
        firestore.collection(PUBLICATIONS_COLLECTION)
            .add(publication.toMap())
            .await()
    }

    /** Alterna a curtida do usuário na publicação com arrayUnion/arrayRemove (operação atômica). */
    suspend fun setLiked(publicationId: String, uid: String, liked: Boolean) {
        val update = if (liked) FieldValue.arrayUnion(uid) else FieldValue.arrayRemove(uid)
        firestore.collection(PUBLICATIONS_COLLECTION)
            .document(publicationId)
            .update("likedBy", update)
            .await()
    }

    /** Observa os comentários de uma publicação, do mais antigo para o mais novo. */
    fun observeComments(publicationId: String): Flow<List<Comment>> = callbackFlow {
        val registration = firestore.collection(PUBLICATIONS_COLLECTION)
            .document(publicationId)
            .collection(COMMENTS_SUBCOLLECTION)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { registration.remove() }
    }

    /** Cria o comentário e incrementa o contador denormalizado na publicação. */
    suspend fun addComment(publicationId: String, comment: Comment) {
        val publicationRef = firestore.collection(PUBLICATIONS_COLLECTION).document(publicationId)
        publicationRef.collection(COMMENTS_SUBCOLLECTION)
            .add(comment.toMap())
            .await()
        publicationRef.update("commentsCount", FieldValue.increment(1)).await()
    }

    /** Alterna a curtida do usuário em um comentário específico. */
    suspend fun setCommentLiked(publicationId: String, commentId: String, uid: String, liked: Boolean) {
        val update = if (liked) FieldValue.arrayUnion(uid) else FieldValue.arrayRemove(uid)
        firestore.collection(PUBLICATIONS_COLLECTION)
            .document(publicationId)
            .collection(COMMENTS_SUBCOLLECTION)
            .document(commentId)
            .update("likedBy", update)
            .await()
    }
}

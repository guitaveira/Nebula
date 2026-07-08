package com.duo.nebula.data.remote

import com.duo.nebula.data.model.Follow
import com.duo.nebula.data.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val PROFILES_COLLECTION = "profiles"
private const val FOLLOWS_COLLECTION = "follows"

@Singleton
class ProfileRemoteSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    /** Cria o documento de perfil no momento do cadastro. */
    suspend fun createProfile(profile: UserProfile) {
        firestore.collection(PROFILES_COLLECTION)
            .document(profile.uid)
            .set(profile.toMap())
            .await()
    }

    suspend fun fetchProfile(uid: String): UserProfile? {
        val snapshot = firestore.collection(PROFILES_COLLECTION)
            .document(uid)
            .get()
            .await()
        return snapshot.toObject(UserProfile::class.java)
    }

    suspend fun updateProfile(uid: String, fields: Map<String, Any>) {
        firestore.collection(PROFILES_COLLECTION)
            .document(uid)
            .update(fields)
            .await()
    }

    /** Envia a foto de perfil para o Storage e retorna a URL pública de download. */
    suspend fun uploadProfilePhoto(uid: String, bytes: ByteArray): String {
        val ref = storage.reference.child("profile_photos/$uid/avatar.jpg")
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    /** Observa em tempo real se "followerId" segue "followedId". */
    fun observeIsFollowing(followerId: String, followedId: String): Flow<Boolean> = callbackFlow {
        val registration = firestore.collection(FOLLOWS_COLLECTION)
            .document(Follow.docId(followerId, followedId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.exists() == true)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Passa a seguir "followedId": cria a aresta em "follows" e incrementa,
     * em lote (write batch), os contadores desnormalizados dos dois perfis.
     */
    suspend fun follow(followerId: String, followedId: String) {
        val followRef = firestore.collection(FOLLOWS_COLLECTION)
            .document(Follow.docId(followerId, followedId))
        val followerProfileRef = firestore.collection(PROFILES_COLLECTION).document(followerId)
        val followedProfileRef = firestore.collection(PROFILES_COLLECTION).document(followedId)

        firestore.runBatch { batch ->
            batch.set(followRef, Follow(followerId, followedId).toMap())
            batch.update(followedProfileRef, "followersCount", FieldValue.increment(1))
            batch.update(followerProfileRef, "followingCount", FieldValue.increment(1))
        }.await()
    }

    /** Deixa de seguir "followedId": remove a aresta e decrementa os contadores. */
    suspend fun unfollow(followerId: String, followedId: String) {
        val followRef = firestore.collection(FOLLOWS_COLLECTION)
            .document(Follow.docId(followerId, followedId))
        val followerProfileRef = firestore.collection(PROFILES_COLLECTION).document(followerId)
        val followedProfileRef = firestore.collection(PROFILES_COLLECTION).document(followedId)

        firestore.runBatch { batch ->
            batch.delete(followRef)
            batch.update(followedProfileRef, "followersCount", FieldValue.increment(-1))
            batch.update(followerProfileRef, "followingCount", FieldValue.increment(-1))
        }.await()
    }
}

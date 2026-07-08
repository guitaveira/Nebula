package com.duo.nebula.data.model

/**
 * Representa o documento de perfil de um usuário na coleção "profiles" do Firestore.
 * O id do documento é sempre igual ao uid do Firebase Authentication.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Contadores desnormalizados, mantidos em sincronia com a coleção "follows"
    // toda vez que alguém segue/deixa de seguir este usuário.
    val followersCount: Long = 0,
    val followingCount: Long = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "bio" to bio,
        "photoUrl" to photoUrl,
        "createdAt" to createdAt,
        "followersCount" to followersCount,
        "followingCount" to followingCount
    )
}

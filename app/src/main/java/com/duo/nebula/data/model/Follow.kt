package com.duo.nebula.data.model

/**
 * Representa uma aresta na coleção "follows": um documento por relação de
 * "segue". O id do documento é sempre "{followerId}_{followedId}" para que
 * seja trivial checar/apagar sem precisar de uma query.
 */
data class Follow(
    val followerId: String = "",
    val followedId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "followerId" to followerId,
        "followedId" to followedId,
        "createdAt" to createdAt
    )

    companion object {
        fun docId(followerId: String, followedId: String) = "${followerId}_${followedId}"
    }
}

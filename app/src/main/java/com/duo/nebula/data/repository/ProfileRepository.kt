package com.duo.nebula.data.repository

import com.duo.nebula.data.model.UserProfile
import com.duo.nebula.data.remote.ProfileRemoteSource
import com.duo.nebula.util.NebulaResult
import com.duo.nebula.util.mapFirebaseError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileRemoteSource: ProfileRemoteSource
) {

    suspend fun getProfile(uid: String): NebulaResult<UserProfile> {
        return try {
            val profile = profileRemoteSource.fetchProfile(uid)
            if (profile != null) {
                NebulaResult.Success(profile)
            } else {
                NebulaResult.Error("Perfil não encontrado.")
            }
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun updateBio(uid: String, newBio: String): NebulaResult<Unit> {
        val trimmed = newBio.trim()
        if (trimmed.length > 150) {
            return NebulaResult.Error("A bio deve ter no máximo 150 caracteres.")
        }
        return try {
            profileRemoteSource.updateProfile(uid, mapOf("bio" to trimmed))
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun updateDisplayName(uid: String, newName: String): NebulaResult<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            return NebulaResult.Error("O nome de usuário não pode ficar em branco.")
        }
        return try {
            profileRemoteSource.updateProfile(uid, mapOf("displayName" to trimmed))
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    suspend fun updatePhoto(uid: String, photoBytes: ByteArray): NebulaResult<String> {
        return try {
            val url = profileRemoteSource.uploadProfilePhoto(uid, photoBytes)
            profileRemoteSource.updateProfile(uid, mapOf("photoUrl" to url))
            NebulaResult.Success(url)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    /** Observa em tempo real se o usuário atual segue o perfil visualizado. */
    fun observeIsFollowing(followerId: String, followedId: String): Flow<Boolean> {
        return profileRemoteSource.observeIsFollowing(followerId, followedId)
    }

    /** Alterna seguir/deixar de seguir um perfil. */
    suspend fun toggleFollow(followerId: String, followedId: String, follow: Boolean): NebulaResult<Unit> {
        return try {
            if (follow) {
                profileRemoteSource.follow(followerId, followedId)
            } else {
                profileRemoteSource.unfollow(followerId, followedId)
            }
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }
}

package com.duo.nebula.data.repository

import com.duo.nebula.data.model.UserProfile
import com.duo.nebula.data.remote.AuthRemoteSource
import com.duo.nebula.data.remote.ProfileRemoteSource
import com.duo.nebula.util.NebulaResult
import com.duo.nebula.util.mapFirebaseError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authRemoteSource: AuthRemoteSource,
    private val profileRemoteSource: ProfileRemoteSource
) {

    val currentUserId: String?
        get() = authRemoteSource.currentUser?.uid

    val isUserLoggedIn: Boolean
        get() = authRemoteSource.currentUser != null

    suspend fun login(email: String, password: String): NebulaResult<Unit> {
        return try {
            authRemoteSource.signIn(email.trim(), password)
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    /**
     * Cria a conta de autenticação e, na sequência, o documento de perfil
     * correspondente no Firestore — ambos precisam ser bem-sucedidos para
     * que o cadastro seja considerado completo.
     */
    suspend fun register(name: String, email: String, password: String): NebulaResult<Unit> {
        return try {
            val firebaseUser = authRemoteSource.signUp(email.trim(), password)
            val profile = UserProfile(
                uid = firebaseUser.uid,
                displayName = name.trim(),
                email = email.trim(),
                bio = "",
                photoUrl = ""
            )
            profileRemoteSource.createProfile(profile)
            NebulaResult.Success(Unit)
        } catch (e: Exception) {
            NebulaResult.Error(mapFirebaseError(e))
        }
    }

    fun logout() {
        authRemoteSource.signOut()
    }
}

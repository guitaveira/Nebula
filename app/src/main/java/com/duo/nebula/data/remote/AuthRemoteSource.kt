package com.duo.nebula.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Camada fina sobre o FirebaseAuth: expõe apenas as operações que o app
 * precisa (login, cadastro, sessão atual e logout).
 */
@Singleton
class AuthRemoteSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: error("Não foi possível autenticar o usuário.")
    }

    suspend fun signUp(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: error("Não foi possível criar a conta.")
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

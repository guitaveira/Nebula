package com.duo.nebula.util

/**
 * Wrapper simples para representar o resultado de operações assíncronas
 * (autenticação, leitura/escrita no Firestore, upload no Storage) sem
 * expor exceções cruas para a camada de UI.
 */
sealed class NebulaResult<out T> {
    data class Success<T>(val data: T) : NebulaResult<T>()
    data class Error(val message: String) : NebulaResult<Nothing>()
}

/** Traduz exceções comuns do Firebase em mensagens amigáveis, em português. */
fun mapFirebaseError(throwable: Throwable): String {
    val raw = throwable.message ?: ""
    return when {
        raw.contains("email address is already in use", ignoreCase = true) ->
            "Este e-mail já está cadastrado. Tente entrar em vez de criar uma conta nova."
        raw.contains("password is invalid", ignoreCase = true) ||
            raw.contains("no user record", ignoreCase = true) ->
            "E-mail ou senha incorretos."
        raw.contains("badly formatted", ignoreCase = true) ->
            "Digite um e-mail válido."
        raw.contains("network error", ignoreCase = true) ->
            "Falha de conexão. Verifique sua internet e tente novamente."
        raw.contains("at least 6 characters", ignoreCase = true) ->
            "A senha precisa ter pelo menos 6 caracteres."
        else -> "Algo deu errado. Tente novamente em instantes."
    }
}

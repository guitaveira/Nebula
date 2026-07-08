package com.duo.nebula.navigation

/** Rotas de navegação do app, centralizadas para evitar strings soltas pelo código. */
object NebulaDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FEED = "feed"
    const val CREATE_PUBLICATION = "create_publication"
    const val PROFILE = "profile"

    private const val AUTHOR_PROFILE_ROUTE = "author_profile/{uid}"
    const val AUTHOR_PROFILE = AUTHOR_PROFILE_ROUTE

    /** Monta a rota de perfil de um autor específico a partir do feed. */
    fun authorProfile(uid: String) = "author_profile/$uid"
}

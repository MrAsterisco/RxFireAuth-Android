package io.github.mrasterisco.rxfireauth.handlers.apple

internal data class SignInWithAppleConfiguration(
    val clientId: String,
    val redirectUri: String,
    val scopes: List<String>
)
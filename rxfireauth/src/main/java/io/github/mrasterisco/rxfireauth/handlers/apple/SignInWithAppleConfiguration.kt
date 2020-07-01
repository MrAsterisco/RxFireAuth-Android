package io.github.mrasterisco.rxfireauth.handlers.apple

data class SignInWithAppleConfiguration(
    val clientId: String,
    val redirectUri: String,
    val scopes: List<String>
) {

    class Builder {
        private lateinit var clientId: String
        private lateinit var redirectUri: String
        private lateinit var scopes: List<String>

        fun clientId(clientId: String) = apply {
            this.clientId = clientId
        }

        fun redirectUri(redirectUri: String) = apply {
            this.redirectUri = redirectUri
        }

        fun scope(scopes: List<String>) = apply {
            this.scopes = scopes
        }

        fun build() = SignInWithAppleConfiguration(clientId, redirectUri, scopes)
    }
}
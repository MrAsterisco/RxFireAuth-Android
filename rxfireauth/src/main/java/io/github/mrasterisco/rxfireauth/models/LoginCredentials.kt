package io.github.mrasterisco.rxfireauth.models

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

public data class LoginCredentials(

    public var idToken: String,
    public var accessToken: String?,
    public var fullName: String?,
    public var email: String,
    public var password: String?,
    public var provider: Provider,
    public var nonce: String?

) {

    public fun toAuthCredentials(): AuthCredential {
        return when (provider) {
            Provider.Password ->
                EmailAuthProvider.getCredential(email, password ?: "")
            Provider.Apple ->
                OAuthProvider
                    .newCredentialBuilder(provider.identifier)
                    .setIdTokenWithRawNonce(idToken, nonce)
                    .build()
            Provider.Google ->
                GoogleAuthProvider.getCredential(idToken, accessToken)
        }
    }

}
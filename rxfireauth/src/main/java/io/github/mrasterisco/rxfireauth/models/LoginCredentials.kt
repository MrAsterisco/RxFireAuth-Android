package io.github.mrasterisco.rxfireauth.models

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import io.github.mrasterisco.rxfireauth.exceptions.MigrationRequiredException

/**
 * This class represents a set of credentials
 * used to perform a sign in with a specific authentication provider.
 *
 * Instances of this class are returned when a recoverable error,
 * such as [MigrationRequiredException], occurs during a sign in.
 *
 * You shouldn't need to to inspect the content of this class.
 * Its main purpose is to temporary store credentials in order to
 * continue the login action when your client has handled the error.
 */
@Suppress("RedundantVisibilityModifier")
public data class LoginCredentials(

    /**
     * Get or set the ID token.
     */
    public var idToken: String,
    /**
     * Get or set the access token.
     */
    public var accessToken: String?,
    /**
     * Get or set the user full name.
     */
    public var fullName: String?,
    /**
     * Get or set the user email.
     */
    public var email: String,
    /**
     * Get or set the user password.
     */
    public var password: String?,
    /**
     * Get or set the authentication provider.
     */
    public var provider: Provider,
    /**
     * Get or set the nonce.
     */
    public var nonce: String?

) {

    /**
     * Convert this instance to a [AuthCredential] instance.
     *
     * @return The Firebase representation of this instance.
     */
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
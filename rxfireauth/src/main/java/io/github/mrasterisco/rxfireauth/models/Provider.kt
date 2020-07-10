package io.github.mrasterisco.rxfireauth.models

/**
 * A provider represents a supported authentication provider.
 */
@Suppress("RedundantVisibilityModifier")
public enum class Provider(public val identifier: String) {

    /**
     * Email & Password.
     */
    Password("password"),

    /**
     * Sign in with Apple.
     */
    Apple("apple.com"),

    /**
     * Google Sign In
     */
    Google("google.com");

    companion object {

        /**
         * Instantiate a new authentication provider
         * from the passed [identifier].
         *
         * @param identifier An identifier.
         * @return A [Provider] (if any exists with the specified identifier).
         */
        fun fromIdentifier(identifier: String): Provider? {
            return when (identifier) {
                Password.identifier -> Password
                Apple.identifier -> Apple
                Google.identifier -> Google
                else -> null
            }
        }

    }

}
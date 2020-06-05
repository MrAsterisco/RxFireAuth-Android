package io.github.mrasterisco.rxfireauth.models

public enum class Provider(public val identifier: String) {

    Password("password"),
    Apple("apple.com"),
    Google("google.com");

    companion object {

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
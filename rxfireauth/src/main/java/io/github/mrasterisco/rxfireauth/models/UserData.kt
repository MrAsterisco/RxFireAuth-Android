package io.github.mrasterisco.rxfireauth.models

import com.google.firebase.auth.FirebaseUser

public data class UserData internal constructor(

    public val id: String?,
    public val email: String?,
    public var displayName: String?,
    public val isAnonymous: Boolean,
    public val authenticationProviders: List<Provider>

) {

    constructor(user: FirebaseUser) : this(
        user.uid,
        user.email,
        user.displayName,
        user.isAnonymous,
        user.providerData.mapNotNull { Provider.fromIdentifier(it.providerId) }
    )

    companion object {

        val empty = UserData(null, null, null, false, emptyList())

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserData

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    val isValid: Boolean
        get() = id?.isNotBlank() == true

}
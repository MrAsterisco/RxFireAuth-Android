package io.github.mrasterisco.rxfireauth.models

import com.google.firebase.auth.FirebaseUser
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager

/**
 * A User.
 *
 * This class usually inherits data from a Firebase User.
 * You cannot instantiate this class directly. Use [IUserManager]
 * implementations to get a user.
 */
@Suppress("RedundantVisibilityModifier")
public data class UserData internal constructor(

    /**
     * Get the ID.
     *
     * Corresponds to [FirebaseUser.getUid()].
     */
    public val id: String?,
    /**
     * Get the email.
     *
     * Corresponds to [FirebaseUser.getEmail()].
     */
    public val email: String?,
    /**
     * Get the user display name.
     *
     * Corresponds to [FirebaseUser.getDisplayName()].
     */
    public var displayName: String?,
    /**
     * Get if this is anonymous user.
     *
     * Corresponds to [FirebaseUser.isAnonymous()].
     */
    public val isAnonymous: Boolean,
    /**
     * Get a list of providers that this user has connected.
     */
    public val authenticationProviders: List<Provider>

) {

    /**
     * Initialize a new instance using data from the passed Firebase User.
     *
     * @param user A Firebase User.
     */
    constructor(user: FirebaseUser) : this(
        user.uid,
        user.email,
        user.displayName,
        user.isAnonymous,
        user.providerData.mapNotNull { Provider.fromIdentifier(it.providerId) }
    )

    companion object {

        /**
         * Get an empty user.
         *
         * An empty user has no [id].
         */
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

    /**
     * Get if this user is valid or not.
     */
    val isValid: Boolean
        get() = id?.isNotBlank() == true

}
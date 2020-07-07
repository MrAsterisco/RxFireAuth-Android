package io.github.mrasterisco.rxfireauth.models

/**
 * A login descriptor represents
 * the result of a login action.
 *
 * Instances of this class are returned from all the functions
 * that perform a sign in.
 */
@Suppress("RedundantVisibilityModifier")
public data class LoginDescriptor(

    /**
     * Get the user full name.
     *
     * This field inherits its value from the sign in method.
     * Some sign in methods don't return this information.
     */
    public val fullName: String?,
    /**
     * Get if this sign in operation requires a data migration.
     *
     * This property holds the same value that you have passed
     * to the "allowMigration" parameter of all the functions that
     * perform a sign in.
     *
     * You can use this value to know if your code actually has to perform
     * a data migration. If true, you should detach all data
     * from [oldUserId] and attach it to [newUserId].
     */
    public val performMigration: Boolean,
    public val oldUserId: String?,
    public val newUserId: String?

)
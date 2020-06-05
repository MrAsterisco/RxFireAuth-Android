package io.github.mrasterisco.rxfireauth.models

public data class LoginDescriptor(

    public val fullName: String?,
    public val performMigration: Boolean,
    public val oldUserId: String?,
    public val newUserId: String?

)
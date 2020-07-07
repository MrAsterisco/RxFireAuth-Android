package io.github.mrasterisco.rxfireauth.exceptions

import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import java.lang.Exception
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager

/**
 * The action would require to migrate the current user data to a new account.
 * Use the passed login credentials to continue signing-in when ready by calling [IUserManager.loginWithCredentials].
 *
 * @property loginCredentials Use these login credentials to continue signing-in when ready.
 */
class MigrationRequiredException(public val loginCredentials: LoginCredentials?): Exception("Proceeding with this action requires confirmation to migrate data from a user account to another.")
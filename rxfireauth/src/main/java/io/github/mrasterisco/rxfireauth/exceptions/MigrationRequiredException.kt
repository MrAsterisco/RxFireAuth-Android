package io.github.mrasterisco.rxfireauth.exceptions

import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import java.lang.Exception

class MigrationRequiredException(public val loginCredentials: LoginCredentials?): Exception("Proceeding with this action requires confirmation to migrate data from a user account to another.")
package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The specified credential is either expired or invalid.
 */
class InvalidCredentialException: IllegalArgumentException("The specified credential is invalid.")
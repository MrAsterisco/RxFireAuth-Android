package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The specified user cannot be found.
 */
class UnexistingUserException: IllegalArgumentException("The specified user cannot be found.")
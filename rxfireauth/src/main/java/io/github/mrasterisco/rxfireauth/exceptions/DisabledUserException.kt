package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

/**
 * The specified user is disabled.
 */
class DisabledUserException: IllegalStateException("The specified user is disabled.")
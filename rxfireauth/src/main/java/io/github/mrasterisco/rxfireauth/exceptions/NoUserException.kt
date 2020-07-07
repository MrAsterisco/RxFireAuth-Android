package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * There is no user associated to perform the requested action.
 */
class NoUserException: IllegalArgumentException("This action requires a logged-in user.")
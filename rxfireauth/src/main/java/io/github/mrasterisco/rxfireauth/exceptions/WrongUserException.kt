package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

/**
 * The requested action would target a different user than the one currently signed-in.
 */
class WrongUserException: IllegalStateException("You are authenticating with a different user.")
package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

/**
 * The requested action cannot be performed because there is already an anonymous user logged-in.
 */
class AlreadyAnonymousException: IllegalStateException("There is already an anonymous user logged-in.")
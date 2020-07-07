package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * There is already another user logged-in.
 */
class AlreadyLoggedInException: IllegalArgumentException("There is already a non-anonymous user logged-in.")
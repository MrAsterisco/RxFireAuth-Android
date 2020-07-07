package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The specified password is invalid.
 */
class WrongPasswordException: IllegalArgumentException("The specified password is invalid.")
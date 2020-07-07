package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The provided email is not valid.
 */
class InvalidEmailException: IllegalArgumentException("The provided email address is invalid.")
package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The specified email is already in use in another account.
 */
class EmailAlreadyInUseException: IllegalArgumentException("This email address is already registered with another account.")
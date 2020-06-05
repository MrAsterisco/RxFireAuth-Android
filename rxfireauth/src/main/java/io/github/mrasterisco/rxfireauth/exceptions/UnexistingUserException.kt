package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class UnexistingUserException: IllegalArgumentException("The specified user cannot be found.")
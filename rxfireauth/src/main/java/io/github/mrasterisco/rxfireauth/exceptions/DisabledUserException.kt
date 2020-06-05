package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

class DisabledUserException: IllegalStateException("The specified user is disabled.")
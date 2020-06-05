package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

class AlreadyAnonymousException: IllegalStateException("There is already an anonymous user logged-in.")
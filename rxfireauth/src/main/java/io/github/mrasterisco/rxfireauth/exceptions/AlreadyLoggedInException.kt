package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class AlreadyLoggedInException: IllegalArgumentException("There is already a non-anonymous user logged-in.")
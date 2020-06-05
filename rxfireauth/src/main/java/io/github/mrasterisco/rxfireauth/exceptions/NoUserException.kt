package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class NoUserException: IllegalArgumentException("This action requires a logged-in user.")
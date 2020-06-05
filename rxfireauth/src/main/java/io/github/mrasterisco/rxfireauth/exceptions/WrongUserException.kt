package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

class WrongUserException: IllegalStateException("You are authenticating with a different user.")
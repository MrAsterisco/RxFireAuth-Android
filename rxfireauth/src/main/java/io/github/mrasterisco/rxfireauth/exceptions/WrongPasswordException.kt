package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class WrongPasswordException: IllegalArgumentException("The specified password is invalid.")
package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class EmailAlreadyInUseException: IllegalArgumentException("This email address is already registered with another account.")
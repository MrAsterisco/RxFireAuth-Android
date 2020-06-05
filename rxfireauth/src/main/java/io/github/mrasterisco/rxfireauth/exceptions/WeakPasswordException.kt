package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class WeakPasswordException(reason: String?): IllegalArgumentException("The provided password does not satisfy the security requirements: ${reason ?: "please try again"}.")
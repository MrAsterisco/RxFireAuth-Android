package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The specified password does not satisfy the basic security requirements.
 *
 * @property reason: A more detailed explanation of what is missing from the password.
 */
class WeakPasswordException(val reason: String?): IllegalArgumentException("The provided password does not satisfy the security requirements: ${reason ?: "please try again"}.")
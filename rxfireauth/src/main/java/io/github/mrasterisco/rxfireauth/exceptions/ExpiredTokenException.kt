package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalStateException

/**
 * The user token has expired.
 */
class ExpiredTokenException: IllegalStateException("The credential stored on this device are no longer valid. Please re-authenticate.")
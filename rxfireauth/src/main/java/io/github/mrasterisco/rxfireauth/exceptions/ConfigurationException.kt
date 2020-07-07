package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.RuntimeException

/**
 * The provided Firebase configuration is invalid.
 */
class ConfigurationException: RuntimeException("There is an error in your Firebase Console configuration. The requested login provider may be disabled.")
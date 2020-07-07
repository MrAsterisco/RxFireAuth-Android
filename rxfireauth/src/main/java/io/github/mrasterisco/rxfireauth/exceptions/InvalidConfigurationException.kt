package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.RuntimeException

/**
 * The provided Firebase configuration is invalid.
 */
class InvalidConfigurationException: RuntimeException("There is an error in your app configuration.")
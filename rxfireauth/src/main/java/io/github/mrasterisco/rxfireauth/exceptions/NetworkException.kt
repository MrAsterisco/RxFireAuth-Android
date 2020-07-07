package io.github.mrasterisco.rxfireauth.exceptions

import java.io.IOException

/**
 * An error occurred while reaching Firebase servers.
 */
class NetworkException: IOException("A network error occurred.")
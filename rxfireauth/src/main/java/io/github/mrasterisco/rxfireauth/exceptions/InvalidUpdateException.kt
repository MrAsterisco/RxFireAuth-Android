package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The update cannot be performed because of invalid data.
 */
class InvalidUpdateException: IllegalArgumentException("This update cannot be performed.")
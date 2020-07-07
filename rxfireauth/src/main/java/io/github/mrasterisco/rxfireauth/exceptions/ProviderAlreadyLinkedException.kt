package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

/**
 * The specified provider is already linked with this user.
 */
class ProviderAlreadyLinkedException: IllegalArgumentException("This login provider is already linked.")
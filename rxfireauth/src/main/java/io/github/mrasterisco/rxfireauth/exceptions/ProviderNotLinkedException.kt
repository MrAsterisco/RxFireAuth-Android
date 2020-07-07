package io.github.mrasterisco.rxfireauth.exceptions

/**
 * The specified provider is not linked with this user.
 */
class ProviderNotLinkedException: IllegalArgumentException("This login provider is not linked.")
package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.IllegalArgumentException

class ProviderAlreadyLinkedException: IllegalArgumentException("This login provider is already linked.")
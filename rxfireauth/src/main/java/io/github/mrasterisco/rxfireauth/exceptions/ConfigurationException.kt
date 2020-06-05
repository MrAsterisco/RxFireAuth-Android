package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.RuntimeException

class ConfigurationException: RuntimeException("There is an error in your Firebase Console configuration. The requested login provider may be disabled.")
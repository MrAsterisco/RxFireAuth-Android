package io.github.mrasterisco.rxfireauth.handlers.google

internal typealias GoogleSignInCompletionHandler = (idToken: String?, accessToken: String?, email: String?, fullName: String?, error: Throwable?) -> Unit
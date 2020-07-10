package io.github.mrasterisco.rxfireauth.handlers.apple

internal typealias SignInWithAppleCompletionHandler = (idToken: String?, nonce: String?, displayName: String?, email: String?, error: Throwable?) -> Unit
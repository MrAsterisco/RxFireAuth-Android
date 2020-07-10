package io.github.mrasterisco.rxfireauth.handlers.apple

interface SignInWithAppleResult

internal data class SignInWithAppleSuccessDescriptor(val idToken: String, val nonce: String, val email: String, val name: String?) : SignInWithAppleResult
internal data class SignInWithAppleFailureDescriptor(val error: Throwable) : SignInWithAppleResult
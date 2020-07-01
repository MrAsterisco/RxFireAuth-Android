package io.github.mrasterisco.rxfireauth.handlers.apple

interface SignInWithAppleResult

data class SignInWithAppleSuccessDescriptor(val idToken: String, val nonce: String) : SignInWithAppleResult
data class SignInWithAppleFailureDescriptor(val error: Throwable) : SignInWithAppleResult
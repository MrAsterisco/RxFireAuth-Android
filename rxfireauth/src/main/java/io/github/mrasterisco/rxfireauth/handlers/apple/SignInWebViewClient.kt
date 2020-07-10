package io.github.mrasterisco.rxfireauth.handlers.apple

import android.webkit.WebView
import android.webkit.WebViewClient

internal class SignInWebViewClient(
    private val attempt: SignInWithAppleInternalHandler.AuthenticationAttempt,
    private val callback: (SignInWithAppleResult) -> Unit,
    private val firebaseJavaScriptReader: FirebaseJavaScriptReader
) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        if (!url.contains(attempt.redirectUri)) return

        firebaseJavaScriptReader.completionHandler = {
            if (firebaseJavaScriptReader.idToken.isNotBlank()) {
                callback(SignInWithAppleSuccessDescriptor(firebaseJavaScriptReader.idToken, attempt.secureRandomString, firebaseJavaScriptReader.email, firebaseJavaScriptReader.name))
            } else {
                callback(SignInWithAppleFailureDescriptor(IllegalArgumentException("ID Token is null or blank.")))
            }
        }
        view.loadUrl("javascript:window.Reader.read(POST_BODY);")
    }

}
package io.github.mrasterisco.rxfireauth.handlers.apple

import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import java.lang.IllegalArgumentException

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SignInWebViewClient(
    private val attempt: SignInWithAppleInternalHandler.AuthenticationAttempt,
    private val callback: (SignInWithAppleResult) -> Unit,
    private val javaScriptInterface: JavaScriptInterface
) : WebViewClient() {

    override fun onPageFinished(view: WebView, url: String) {
        if (!url.contains(attempt.redirectUri)) return

        view.loadUrl("javascript:window.Reader.read(POST_BODY);")

        if (javaScriptInterface.idToken.isNotBlank()) {
            callback(SignInWithAppleSuccessDescriptor(javaScriptInterface.idToken, attempt.secureRandomString))
        } else {
            callback(SignInWithAppleFailureDescriptor(IllegalArgumentException("ID Token is null or blank.")))
        }
    }

}
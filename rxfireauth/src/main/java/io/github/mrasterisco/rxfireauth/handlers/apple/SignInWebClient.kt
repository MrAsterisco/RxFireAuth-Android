package io.github.mrasterisco.rxfireauth.handlers.apple

import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SignInWebViewClient(
    private val attempt: SignInWithAppleService.AuthenticationAttempt,
    private val callback: (SignInWithAppleResult) -> Unit
) : WebViewClient() {

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        handleNewUrl(Uri.parse(url))
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }

    private fun handleNewUrl(url: Uri) {
        if (!url.toString().contains(attempt.redirectUri)) return



        Log.d("SIGN_IN_WITH_APPLE", "Web view was forwarded to redirect URI")

        val codeParameter = url.getQueryParameter("code")
        val stateParameter = url.getQueryParameter("state")

        when {
            codeParameter == null -> {
                callback(SignInWithAppleResult.Failure(IllegalArgumentException("code not returned")))
            }
            stateParameter != attempt.state -> {
                callback(SignInWithAppleResult.Failure(IllegalArgumentException("state does not match")))
            }
            else -> {
                callback(SignInWithAppleResult.Success(codeParameter))
            }
        }
    }

}
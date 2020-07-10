package io.github.mrasterisco.rxfireauth.handlers.apple

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import java.util.concurrent.CancellationException

internal class SignInWebViewDialogFragment : DialogFragment() {

    companion object {
        private const val AUTHENTICATION_ATTEMPT_KEY = "authenticationAttempt"
        private const val WEB_VIEW_KEY = "webView"

        fun newInstance(authenticationAttempt: SignInWithAppleInternalHandler.AuthenticationAttempt): SignInWebViewDialogFragment {
            val fragment = SignInWebViewDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(AUTHENTICATION_ATTEMPT_KEY, authenticationAttempt)
            }
            return fragment
        }
    }

    private lateinit var authenticationAttempt: SignInWithAppleInternalHandler.AuthenticationAttempt
    private var callback: ((SignInWithAppleResult) -> Unit)? = null

    private val webViewIfCreated: WebView?
        get() = view as? WebView

    fun configure(callback: (SignInWithAppleResult) -> Unit) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticationAttempt = arguments?.getParcelable(AUTHENTICATION_ATTEMPT_KEY)!!
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(false)
            }
            val javaScriptInterface = FirebaseJavaScriptReader()
            addJavascriptInterface(javaScriptInterface, "Reader")
            webViewClient =
                SignInWebViewClient(authenticationAttempt, ::onCallback, javaScriptInterface)
        }

        if (savedInstanceState != null) {
            savedInstanceState.getBundle(WEB_VIEW_KEY)?.run {
                webView.restoreState(this)
            }
        } else {
            webView.loadUrl(authenticationAttempt.authenticationUri)
        }

        return webView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            WEB_VIEW_KEY,
            Bundle().apply {
                webViewIfCreated?.saveState(this)
            }
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCallback(SignInWithAppleFailureDescriptor(CancellationException()))
    }

    private fun onCallback(result: SignInWithAppleResult) {
        dialog?.dismiss()
        val callback = callback
        if (callback == null) {
            Log.e("SIGN_IN_WITH_APPLE", "Callback is not configured")
            return
        }
        callback(result)
    }

}
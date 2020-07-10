package io.github.mrasterisco.rxfireauth.handlers.google

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.github.mrasterisco.rxfireauth.interfaces.ILoginHandler

internal class GoogleSignInHandler(clientId: String, private val activity: Activity, private val requestCode: Int): ILoginHandler {

    private val client: GoogleSignInClient =
        GoogleSignIn.getClient(
            activity, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .requestProfile()
                .build()
        )

    private var completionHandler: GoogleSignInCompletionHandler? = null

    fun signIn(completionHandler: GoogleSignInCompletionHandler?) {
        this.completionHandler = completionHandler
        activity.startActivityForResult(client.signInIntent, requestCode)
    }

    override fun handle(data: Intent): Boolean {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                completionHandler?.invoke(account.idToken ?: "", null, account.email, account.displayName, null)
                return true
            } else {
                throw IllegalArgumentException("Google Sign In returned empty account!")
            }
        } catch (exc: Throwable) {
            completionHandler?.invoke("", null, null, null, exc)
        }

        return false
    }

}
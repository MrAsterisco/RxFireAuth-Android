package io.github.mrasterisco.rxfireauth.handlers.apple

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import java.util.*

internal class SignInWithAppleInternalHandler(
    private val fragmentManager: FragmentManager,
    private val fragmentTag: String,
    private val configuration: SignInWithAppleConfiguration,
    private val callback: (SignInWithAppleResult) -> Unit
) {

    init {
        val fragmentIfShown =
            fragmentManager.findFragmentByTag(fragmentTag) as? SignInWebViewDialogFragment
        fragmentIfShown?.configure(callback)
    }

    internal data class AuthenticationAttempt(
        val authenticationUri: String,
        val redirectUri: String,
        val state: String,
        val secureRandomString: String
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "invalid",
            parcel.readString() ?: "invalid",
            parcel.readString() ?: "invalid",
            parcel.readString() ?: "invalid"
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(authenticationUri)
            parcel.writeString(redirectUri)
            parcel.writeString(state)
            parcel.writeString(secureRandomString)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<AuthenticationAttempt> {

            override fun createFromParcel(parcel: Parcel) = AuthenticationAttempt(parcel)

            override fun newArray(size: Int): Array<AuthenticationAttempt?> = arrayOfNulls(size)

            fun create(
                configuration: SignInWithAppleConfiguration,
                state: String = UUID.randomUUID().toString(),
                nonce: String = String.getSecureRandomString()
            ): AuthenticationAttempt {
                val authenticationUri = Uri
                    .parse("https://appleid.apple.com/auth/authorize")
                    .buildUpon().apply {
                        appendQueryParameter("client_id", configuration.clientId)
                        appendQueryParameter("redirect_uri", configuration.redirectUri)
                        appendQueryParameter("response_type", "code id_token")
                        appendQueryParameter("scope", configuration.scopes.joinToString(" "))
                        appendQueryParameter("response_mode", "form_post")
                        appendQueryParameter("state", state)
                        appendQueryParameter("nonce", nonce.sha256())
                    }
                    .build()
                    .toString()

                return AuthenticationAttempt(authenticationUri, configuration.redirectUri, state, nonce)
            }
        }
    }

    fun show() {
        val fragment = SignInWebViewDialogFragment.newInstance(AuthenticationAttempt.create(configuration))
        fragment.configure(callback)
        fragment.show(fragmentManager, fragmentTag)
    }

}
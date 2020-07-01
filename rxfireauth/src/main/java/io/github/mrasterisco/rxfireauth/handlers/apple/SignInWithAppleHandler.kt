package io.github.mrasterisco.rxfireauth.handlers.apple

import android.os.Build.VERSION_CODES.LOLLIPOP
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity

@RequiresApi(LOLLIPOP)
class SignInWithAppleHandler(private val activity: FragmentActivity, private val serviceId: String, private val redirectUri: String) {

    private val scopes = listOf("name", "email")

    fun signIn() {
        val fragmentTag = "SignInWithAppleButton-SignInWebViewDialogFragment"
        val service = SignInWithAppleInternalHandler(activity.supportFragmentManager, fragmentTag, SignInWithAppleConfiguration(serviceId, redirectUri, scopes)) {
            Log.d("SIGN_IN_WITH_APPLE", it.toString())
        }
        service.show()
    }

}
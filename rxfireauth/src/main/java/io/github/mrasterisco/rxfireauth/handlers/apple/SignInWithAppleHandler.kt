package io.github.mrasterisco.rxfireauth.handlers.apple

import android.content.Intent
import android.os.Build.VERSION_CODES.LOLLIPOP
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import io.github.mrasterisco.rxfireauth.interfaces.ILoginHandler

typealias SignInWithAppleCompletionHandler = (idToken: String?, nonce: String?, displayName: String?, email: String?, error: Throwable?) -> Unit

@RequiresApi(LOLLIPOP)
class SignInWithAppleHandler(private val activity: FragmentActivity, private val serviceId: String, private val redirectUri: String): ILoginHandler {

    private val scopes = listOf("name", "email")

    fun signIn(completionHandler: SignInWithAppleCompletionHandler?) {
        val fragmentTag = "SignInWithAppleButton-SignInWebViewDialogFragment"
        val service = SignInWithAppleInternalHandler(activity.supportFragmentManager, fragmentTag, SignInWithAppleConfiguration(serviceId, redirectUri, scopes)) {
            when (it) {
                is SignInWithAppleSuccessDescriptor -> completionHandler?.invoke(it.idToken, it.nonce, it.name, it.email, null)
                is SignInWithAppleFailureDescriptor -> completionHandler?.invoke(null, null, null, null, it.error)
            }
        }
        service.show()
    }

    override fun handle(data: Intent) = false

}
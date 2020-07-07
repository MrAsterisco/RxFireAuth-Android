package io.github.mrasterisco.rxfireauth.interfaces

import android.content.Intent
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager

/**
 * This interface identifies a login handler object
 * that is used by the library to authenticated with a 3rd-party provider,
 * such as Apple or Google.
 *
 * You will get an instance of this interface when reading the value of
 * [IUserManager.loginHandler]. You can use it to redirect activity results
 * or incoming calls from the system browser, for example, when authenticating with a OAuth
 * provider that redirects directly to your app (such as Google Sign In).
 */
interface ILoginHandler {

    /**
     * Handle the passed [Intent].
     *
     * @param data An [Intent], usually coming from the result of a call to startActivityForResult.
     * @return true if the intent has been handled, false if it should be handled by someone else.
     */
    fun handle(data: Intent): Boolean

}
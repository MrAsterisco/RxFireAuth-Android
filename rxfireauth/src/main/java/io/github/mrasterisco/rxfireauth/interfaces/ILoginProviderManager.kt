package io.github.mrasterisco.rxfireauth.interfaces

import android.app.Activity
import io.github.mrasterisco.rxfireauth.models.LoginDescriptor
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.*

public interface ILoginProviderManager {

    fun signInWithApple(
        activity: Activity,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?,
        locale: Locale? = null
    ): Single<LoginDescriptor>

    fun confirmAuthenticationWithApple(activity: Activity): Completable

}
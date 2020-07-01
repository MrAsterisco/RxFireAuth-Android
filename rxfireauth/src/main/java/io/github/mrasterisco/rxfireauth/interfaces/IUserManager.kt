package io.github.mrasterisco.rxfireauth.interfaces

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import io.github.mrasterisco.rxfireauth.models.LoginDescriptor
import io.github.mrasterisco.rxfireauth.models.UserData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.*

public interface IUserManager {

    public val isLoggedIn: Boolean

    public val isAnonymous: Boolean

    public val user: UserData?

    public val autoupdatingUser: Observable<UserData>


    public fun accountExists(email: CharSequence): Single<Boolean>

    public fun register(email: CharSequence, password: CharSequence): Completable

    public fun loginAnonymously(): Completable

    public fun linkAnonymousAccount(email: CharSequence, password: CharSequence): Completable

    public fun login(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    public fun loginWithoutChecking(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    public fun loginWithCredentials(
        credentials: LoginCredentials,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    public fun logout(resetToAnonymous: Boolean): Completable

    public fun updateUser(user: UserData): Completable

    public fun updateUser(userConfigurationHandler: (UserData) -> UserData): Completable

    public fun updateEmail(newEmail: CharSequence): Completable

    public fun confirmAuthentication(email: CharSequence, password: CharSequence): Completable

    public fun confirmAuthentication(loginCredentials: LoginCredentials): Completable

    public fun deleteUser(resetToAnonymous: Boolean): Completable

    public fun updatePassword(newPassword: CharSequence): Completable

    fun signInWithApple(
        activity: FragmentActivity,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?,
        locale: Locale? = null
    ): Single<LoginDescriptor>

//    fun confirmAuthenticationWithApple(activity: Activity): Completable

}
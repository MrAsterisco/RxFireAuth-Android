package io.github.mrasterisco.rxfireauth.interfaces

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import io.github.mrasterisco.rxfireauth.models.LoginDescriptor
import io.github.mrasterisco.rxfireauth.models.UserData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

public interface IUserManager {

    val loginHandler: ILoginHandler?

    val isLoggedIn: Boolean

    val isAnonymous: Boolean

    val user: UserData?

    val autoupdatingUser: Observable<UserData>

    fun accountExists(email: CharSequence): Single<Boolean>

    fun register(email: CharSequence, password: CharSequence): Completable

    fun loginAnonymously(): Completable

    fun linkAnonymousAccount(email: CharSequence, password: CharSequence): Completable

    fun login(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    fun loginWithoutChecking(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    fun loginWithCredentials(
        credentials: LoginCredentials,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    fun logout(resetToAnonymous: Boolean): Completable

    fun updateUser(user: UserData): Completable

    fun updateUser(userConfigurationHandler: (UserData) -> UserData): Completable

    fun updateEmail(newEmail: CharSequence): Completable

    fun confirmAuthentication(email: CharSequence, password: CharSequence): Completable

    fun confirmAuthenticationWithCredentials(loginCredentials: LoginCredentials): Completable

    fun deleteUser(resetToAnonymous: Boolean): Completable

    fun updatePassword(newPassword: CharSequence): Completable

    fun signInWithApple(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    fun confirmAuthenticationWithApple(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String
    ): Completable

    fun signInWithGoogle(
        activity: Activity,
        clientId: String,
        requestCode: Int,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    fun confirmAuthenticationWithGoogle(
        activity: FragmentActivity,
        clientId: String,
        requestCode: Int
    ): Completable

}
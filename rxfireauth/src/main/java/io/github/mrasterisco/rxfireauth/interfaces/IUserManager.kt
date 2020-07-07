package io.github.mrasterisco.rxfireauth.interfaces

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import io.github.mrasterisco.rxfireauth.exceptions.AlreadyAnonymousException
import io.github.mrasterisco.rxfireauth.exceptions.AlreadyLoggedInException
import io.github.mrasterisco.rxfireauth.exceptions.MigrationRequiredException
import io.github.mrasterisco.rxfireauth.exceptions.NoUserException
import io.github.mrasterisco.rxfireauth.exceptions.ProviderNotLinkedException
import io.github.mrasterisco.rxfireauth.impl.UserManager
import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import io.github.mrasterisco.rxfireauth.models.LoginDescriptor
import io.github.mrasterisco.rxfireauth.models.Provider
import io.github.mrasterisco.rxfireauth.models.UserData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import androidx.fragment.app.DialogFragment

/**
 * This interface defines the public APIs of the main wrapper
 * around the Firebase Authentication SDK.
 *
 * When using the library in your code always make sure to reference this
 * interface instead of the default implementation [UserManager], as
 * this interface will always conform to Semantic Versioning.
 *
 * All methods of this interface are wrapped inside a Rx object that will not execute
 * any code until somebody subscribes to it.
 */
interface IUserManager {

    /**
     * Get the current login handler.
     *
     * This property holds a reference to the handler that is
     * being used during a login operation with multiple steps
     * (such as Google Sign In).
     */
    val loginHandler: ILoginHandler?

    /**
     * Get if there is a currently logged-in user.
     *
     * This property will be `false` even if
     * there is a currently logged-in user, but it is anonymous.
     */
    val isLoggedIn: Boolean

    /**
     * Get if there is an anonymous user logged-in.
     */
    val isAnonymous: Boolean

    /**
     * Get the currently logged-in user or null if no user is logged-in.
     */
    val user: UserData?

    /**
     * Get an Observable that emits a new item every time the logged-in user
     * is updated.
     *
     * Since RxJava does not support emitting null, this Observable will always
     * emit a value, even when there is no user logged-in. To check whether the returned object
     * is a valid user or not, call [UserData.isValid] on it.
     */
    val autoupdatingUser: Observable<UserData>

    /**
     * Verify if an account exists on the server with the passed email address.
     *
     * @param email The account email address.
     * @return A Single that completes with the result of the query on the backend.
     */
    fun accountExists(email: CharSequence): Single<Boolean>

    /**
     * Register a new account on the server with the passed email and password.
     *
     * The resulting Single will emit [AlreadyLoggedInException] if there is already
     * a non-anonymous user logged-in. If the logged-in user is anonymous, this function will
     * call [linkAnonymousAccount] and return that value.
     *
     * After registering, the new user will become the currently logged-in user automatically.
     *
     * @param email The user email address.
     * @param password The user password.
     * @return A Completable action to observe.
     */
    fun register(email: CharSequence, password: CharSequence): Completable

    /**
     * Login an anonymous user.
     *
     * You can use this method to create an anonymous user on the server.
     *
     * The resulting Completable will emit [AlreadyLoggedInException] if there is already
     * a non-anonymous user logged-in. It will also emit [AlreadyAnonymousException] if there is
     * already an anonymous user logged-in.
     *
     * @return A Completable action to observe.
     */
    fun loginAnonymously(): Completable

    /**
     * Convert an anonymous account to a normal user with an email and a password.
     *
     * The resulting Completable will emit [NoUserException] if the currently logged-in user
     * is not anonymous or is null.
     *
     * @param email The user email address.
     * @param password: The user password.
     * @return A Completable action to observe.
     */
    fun linkAnonymousAccount(email: CharSequence, password: CharSequence): Completable

    /**
     * Login the user with the specified email address using the specified password.
     *
     * The resulting Single will emit [AlreadyLoggedInException] if there is already
     * a non-anonymous user logged-in.
     *
     * You can control the migration behavior using [allowMigration], which will be passed back
     * to the caller in [LoginDescriptor.performMigration]; if set to null and a migration is required,
     * the Single will emit [MigrationRequiredException].
     *
     * @param email The user email address.
     * @param password The user password.
     * @param allowMigration An optional boolean that defines the behavior in case there is an anonymous
     *                      user logged-in and the user is trying to login into an existing account.
     * @return A Single that emits errors or a [LoginDescriptor] instance.
     */
    fun login(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    /**
     * Sign in with the passed credentials without first checking if an account
     * with the specified email address exists on the backend.
     *
     * @param email An email address.
     * @param password A password.
     * @param allowMigration An optional boolean that defines the behavior in case there is an anonymous
     *                      user logged-in and the user is trying to login into an existing account.
     * @return A Single to observe for results.
     */
    fun loginWithoutChecking(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    /**
     * Sign in with the passed credentials on a login provider.
     *
     * Use this function to sing in with a provider credentials. In a normal flow,
     * you'll use this function with credentials obtained by one of the "signInWith…" methods.
     *
     * @param credentials Credentials to use to login.
     * @param updateUserDisplayName: If the passed credentials result in a successful login and this is set to `true`, this function will attempt to update the user display name by reading it from the resulting [LoginDescriptor].
     * @param allowMigration An optional boolean that defines the behavior in case there is an anonymous
     *                      user logged-in and the user is trying to login into an existing account.
     * @return A Single to observe for results.
     */
    fun loginWithCredentials(
        credentials: LoginCredentials,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    /**
     * Sign out the currently logged-in user.
     *
     * Using [resetToAnonymous], you can make sure that there is always a user signed in; in fact,
     * if the parameter is set to `true`, this function will call [loginAnonymously] immediately after
     * the logout operation has completed.
     *
     * @param resetToAnonymous If true, after having logged-out successfully, this function will immediately sign in a new anonymous user.
     * @return A Completable action to observe.
     */
    fun logout(resetToAnonymous: Boolean): Completable

    /**
     * Update the currently signed in user taking new values from the passed object.
     *
     * You cannot instantiate a [UserData] instance directly. To pass the parameter to this function,
     * use a value retrieved from [user] or [autoupdatingUser]. To simplify this even further, use [updateUser(userConfigurationHandler:)].
     *
     * This function will not update the user email address, even if it has changed.
     *
     * @see updateUser
     * @param user A user to gather new values from.
     * @return A Completable action to observe.
     */
    fun updateUser(user: UserData): Completable

    /**
     * Update the currently signed in user by retrieving its value and passing it
     * to the [userConfigurationHandler].
     *
     * This function is a shorthand that takes the first value of [autoupdatingUser],
     * maps it by calling [userConfigurationHandler] and passes the result to [updateUser].
     *
     * @param userConfigurationHandler A function that takes a [UserData] instance and returns it with the required changes.
     * @return A Completable action to observe.
     */
    fun updateUser(userConfigurationHandler: (UserData) -> UserData): Completable

    /**
     * Update the email of the currently signed in user.
     *
     * All users have an email address associated, even those that have signed in using a login provider (such as Google).
     * Keep in mind that some login providers may return a relay email which may not be enabled to receive messages.
     *
     * @param newEmail The new email address.
     * @return A Completable action to observe.
     */
    fun updateEmail(newEmail: CharSequence): Completable

    /**
     * Confirm the authentication of the passed credentials with the currently signed in user.
     *
     * You need to confirm the authentication of a user before performing sensitive operations, such
     * as deleting the account, associating a new login provider or changing the email or password.
     *
     * To confirm the authentication with a login provider (such as Google), use the appropriate method in
     * the "confirmAuthenticationWith" family, or confirm the authentication by other means and then call
     * [confirmAuthenticationWithCredentials].
     */
    fun confirmAuthentication(email: CharSequence, password: CharSequence): Completable

    /**
     * Confirm the authentication of the passed credentials with the currently signed in user.
     *
     * @param loginCredentials A representation of the credentials used to login.
     * @return A Completable action to observe.
     */
    fun confirmAuthenticationWithCredentials(loginCredentials: LoginCredentials): Completable

    /**
     * Delete the currently signed in user.
     *
     * This is a sensitive action. If the user hasn't signed in recently, you'll need to
     * confirm the authentication through one of the methods in the "confirmAuthenticationWith…" family.
     *
     * Using [resetToAnonymous], you can make sure that there is always a user signed in; in fact,
     * if the parameter is set to `true`, this function will call [loginAnonymously] immediately after
     * the logout operation has completed.
     *
     */
    fun deleteUser(resetToAnonymous: Boolean): Completable

    /**
     * Update or set the password of the currently signed in user.
     *
     * If the user does not have a password among their [UserData.authenticationProviders],
     * this function will create a new provider using the user email and the specified password.
     * This will basically link the Email & Password authentication to the user.
     * If the user already has [Provider.Password] as an authentication provider, this function
     * will simply update their password.
     *
     * This is a sensitive action. If the user hasn't signed in recently, you'll need to
     * confirm the authentication through one of the methods in the "confirmAuthenticationWith…" family.
     *
     * @param newPassword: The new password.
     * @return A Completable action to observe.
     */
    fun updatePassword(newPassword: CharSequence): Completable

    /**
     * Sign in with Apple in the passed [activity].
     *
     * Sign in with Apple can only be configured by members of the [Apple Developer Program](https://developer.apple.com/programs/).
     *
     * The [serviceId] is the identifier that you have created under "Certificates, Identifiers & Profiles"
     * on the [Apple Developer Portal](https://developer.apple.com). If you don't have one, you must create it before
     * starting to use this function, as well as enabling the appropriate provider on the Firebase Console.
     * You can get further info on the whole procedure on the [Firebase Docs](https://firebase.google.com/docs/auth/android/apple#configure-sign-in-with-apple).
     *
     * Because of limitations on how Apple returns information to the app from Sign in with Apple, this function
     * will make use of a WebView, instead of Chrome Custom Tabs. The user may not be familiar or feel safe with this, so you
     * may want to give an introduction before displaying the Sign in with Apple web page.
     *
     * The Sign in with Apple flow will be different for new users and returning users; as a result, in the latter case,
     * the library will not be able to retrieve the user's display name, as Apple does not provide this information for
     * returning users.
     * Keep in mind that the account you are creating using this function will be linked to the user's Apple ID, but that link will only work
     * in one direction: from Apple to Firebase; if you delete the Firebase account, the user will still find your app
     * in their Apple ID settings, under "Apps Using Your Apple ID".
     *
     * To use Sign in with Apple, your app must comply with specific terms. We strongly suggest you to review them before
     * starting the implementation: you can find those on the [Apple Developer Portal](https://developer.apple.com/sign-in-with-apple/).
     *
     * @param activity An activity that supports showing a [DialogFragment].
     * @param serviceId The identifier of your Service ID on the Apple Developer Portal.
     * @param redirectUri The URI that you have configured on the Service ID on the Apple Developer Portal. You can retrieve it on the Firebase Console, under the Sign in with Apple configuration and it usually looks something like "https://<YOUR PROJECT>.firebaseapp.com/__/auth/handler".
     * @param updateUserDisplayName: If the passed credentials result in a successful login and this is set to `true`, this function will attempt to update the user display name by reading it from the resulting [LoginDescriptor].
     * @param allowMigration An optional boolean that defines the behavior in case there is an anonymous
     *                      user logged-in and the user is trying to login into an existing account.
     * @return A Single that emits errors or a [LoginDescriptor] instance.
     */
    fun signInWithApple(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    /**
     * Confirm the authentication of the currently signed in user with Sign in with Apple.
     *
     * You can use this function to renew the user authentication in order to perform sensitive
     * actions such as updating the password or the deleting the account. The resulting Completable will
     * emit [ProviderNotLinkedException], if the user does not have Sign in with Apple among their [UserData.authenticationProviders].
     *
     * @param activity An activity that supports showing a [DialogFragment].
     * @param serviceId The identifier of your Service ID on the Apple Developer Portal.
     * @param redirectUri The URI that you have configured on the Service ID on the Apple Developer Portal. You can retrieve it on the Firebase Console, under the Sign in with Apple configuration and it usually looks something like "https://<YOUR PROJECT>.firebaseapp.com/__/auth/handler".
     * @return A Completable action to observe.
     */
    fun confirmAuthenticationWithApple(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String
    ): Completable

    /**
     * Sign in with Google in the passed [activity].
     *
     * Before using Google Sign In, you must add your [SHA1 fingerprint](https://developers.google.com/android/guides/client-auth) in your Project Settings; then
     * copy your Web client ID (under Web SDK configuration, in the Authentication tab of your Firebase Console) and use it as [clientId].
     *
     * Google Sign In works by starting a new activity for result from [activity]. This means that the result of the action will be returned to [activity]
     * in the [Activity.onActivityResult] method. For the library to be able to continue the login operation, you must forward that Intent to the current
     * [loginHandler], by calling [ILoginHandler.handle] on it; you can identify the intent, by comparing the request code with the [requestCode] you've passed
     * when calling this function.
     *
     * @param activity The activity over which the Google Sign In UI should be displayed.
     * @param clientId Your client ID.
     * @param requestCode A unique identifier that is returned to your activity in [Activity.onActivityResult]. When you receive this request code, you must forward the Intent to the [loginHandler], by calling [ILoginHandler.handle].
     * @param updateUserDisplayName: If the passed credentials result in a successful login and this is set to `true`, this function will attempt to update the user display name by reading it from the resulting [LoginDescriptor].
     * @param allowMigration An optional boolean that defines the behavior in case there is an anonymous
     *                      user logged-in and the user is trying to login into an existing account.
     * @return A Single that emits errors or a [LoginDescriptor] instance.
     */
    fun signInWithGoogle(
        activity: Activity,
        clientId: String,
        requestCode: Int,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor>

    /**
     * Confirm the authentication of the currently signed in user with Google Sign In.
     *
     * You can use this function to renew the user authentication in order to perform sensitive
     * actions such as updating the password or the deleting the account. The resulting Completable will
     * emit [ProviderNotLinkedException], if the user does not have Google Sign In among their [UserData.authenticationProviders].
     *
     * @param activity The activity over which the Google Sign In UI should be displayed.
     * @param clientId Your client ID:
     * @param requestCode A unique identifier that is returned to your activity in [Activity.onActivityResult]. When you receive this request code, you must forward the Intent to the [loginHandler], by calling [ILoginHandler.handle].
     * @return A Completable action to observe.
     */
    fun confirmAuthenticationWithGoogle(
        activity: FragmentActivity,
        clientId: String,
        requestCode: Int
    ): Completable

}
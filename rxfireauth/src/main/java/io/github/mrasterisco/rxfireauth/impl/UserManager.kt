@file:Suppress("ThrowableNotThrown")

package io.github.mrasterisco.rxfireauth.impl

import android.app.Activity
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.*
import io.github.mrasterisco.rxfireauth.exceptions.*
import io.github.mrasterisco.rxfireauth.handlers.apple.SignInWithAppleHandler
import io.github.mrasterisco.rxfireauth.handlers.google.GoogleSignInHandler
import io.github.mrasterisco.rxfireauth.interfaces.ILoginHandler
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager
import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import io.github.mrasterisco.rxfireauth.models.LoginDescriptor
import io.github.mrasterisco.rxfireauth.models.Provider
import io.github.mrasterisco.rxfireauth.models.UserData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject

class UserManager : IUserManager {

    override var loginHandler: ILoginHandler? = null
        private set

    override val isLoggedIn: Boolean
        get() = FirebaseAuth.getInstance().currentUser.let { it != null && !it.isAnonymous }

    override val isAnonymous: Boolean
        get() = FirebaseAuth.getInstance().currentUser.let { it != null && it.isAnonymous }

    override val user: UserData?
        get() {
            return UserData(FirebaseAuth.getInstance().currentUser ?: return null)
        }

    private val forceRefreshAutoUpdatingUser = BehaviorSubject.createDefault(0)

    override val autoupdatingUser: Observable<UserData>
        get() {
            return Observable.create {
                val listener: (FirebaseAuth) -> Unit = { firebase ->
                    if (firebase.currentUser != null) {
                        it.onNext(UserData(firebase.currentUser!!))
                    } else {
                        it.onNext(UserData.empty)
                    }
                }

                FirebaseAuth.getInstance().addAuthStateListener(listener)

                val subscription = forceRefreshAutoUpdatingUser.subscribe { _ ->
                    val firebase = FirebaseAuth.getInstance()
                    if (firebase.currentUser != null) {
                        it.onNext(UserData(firebase.currentUser!!))
                    } else {
                        it.onNext(UserData.empty)
                    }
                }

                it.setCancellable {
                    FirebaseAuth.getInstance().removeAuthStateListener(listener)
                    subscription.dispose()
                }
            }
        }

    override fun accountExists(email: CharSequence): Single<Boolean> {
        return Single.create {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email.toString())
                .addOnSuccessListener { methods ->
                    if (it.isDisposed) return@addOnSuccessListener
                    it.onSuccess(!methods.signInMethods.isNullOrEmpty())
                }
                .addOnFailureListener { error ->
                    if (it.isDisposed) return@addOnFailureListener
                    it.onError(map(error))
                }
        }
    }

    override fun register(email: CharSequence, password: CharSequence): Completable =
        Completable.defer {
            if (isLoggedIn) return@defer Completable.error(AlreadyLoggedInException())

            if (FirebaseAuth.getInstance().currentUser?.isAnonymous == true) {
                return@defer linkAnonymousAccount(email, password)
            }

            return@defer Completable.create { emitter ->
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email.toString(), password.toString())
                    .addOnSuccessListener {
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.onError(map(it))
                    }
            }
        }

    override fun loginAnonymously(): Completable =
        Completable.defer {
            if (isLoggedIn) return@defer Completable.error(AlreadyLoggedInException())
            if (isAnonymous) return@defer Completable.error(AlreadyAnonymousException())

            return@defer Completable.create { emitter ->
                FirebaseAuth.getInstance().signInAnonymously()
                    .addOnSuccessListener {
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.onError(map(it))
                    }
            }
        }

    private fun linkUserToEmailAndPassword(
        user: FirebaseUser,
        email: CharSequence,
        password: CharSequence
    ): Completable =
        Completable.create { emitter ->
            user.linkWithCredential(
                EmailAuthProvider.getCredential(
                    email.toString(),
                    password.toString()
                )
            )
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(map(it))
                }
        }

    override fun linkAnonymousAccount(email: CharSequence, password: CharSequence): Completable =
        Completable.defer {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null || !user.isAnonymous) return@defer Completable.error(NoUserException())

            return@defer linkUserToEmailAndPassword(user, email, password)
        }

    override fun login(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor> =
        Single.defer {
            if (isLoggedIn) return@defer Single.error<LoginDescriptor?>(AlreadyLoggedInException())

            return@defer accountExists(email)
                .flatMap { accountExists ->
                    if (accountExists) {
                        return@flatMap loginWithoutChecking(email, password, allowMigration)
                    } else {
                        return@flatMap register(email, password)
                            .andThen(
                                Single.just(
                                    LoginDescriptor(null, false, null, user?.id)
                                )
                            )
                    }
                }
        }

    override fun loginWithoutChecking(
        email: CharSequence,
        password: CharSequence,
        allowMigration: Boolean?
    ): Single<LoginDescriptor> =
        Single.create { emitter ->
            var oldUserId: String? = null

            val successHandler: () -> Unit = {
                val newUser = FirebaseAuth.getInstance().currentUser
                if (newUser != null) {
                    emitter.onSuccess(
                        LoginDescriptor(
                            null,
                            allowMigration ?: false,
                            oldUserId,
                            newUser.uid
                        )
                    )
                } else {
                    @Suppress("ThrowableNotThrown")
                    emitter.onError(NoUserException())
                }
            }

            val failureHandler: (Throwable) -> Unit = {
                emitter.onError(map(it))
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && currentUser.isAnonymous) {
                if (allowMigration == null) {
                    emitter.onError(MigrationRequiredException(null))
                    return@create
                }

                oldUserId = currentUser.uid

                currentUser.delete()
                    .addOnSuccessListener {
                        signInWithCredentials(
                            EmailAuthProvider.getCredential(email.toString(), password.toString()),
                            successHandler,
                            failureHandler
                        )
                    }
                    .addOnFailureListener {
                        emitter.onError(map(it))
                    }
            } else {
                signInWithCredentials(
                    EmailAuthProvider.getCredential(email.toString(), password.toString()),
                    successHandler,
                    failureHandler
                )
            }
        }

    override fun loginWithCredentials(
        credentials: LoginCredentials,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor> {
        val s: Single<LoginDescriptor> = Single.create { emitter ->
            val firebaseCredentials = credentials.toAuthCredentials()

            var oldUserId: String? = null

            val successHandler: () -> Unit = {
                val newUser = FirebaseAuth.getInstance().currentUser
                if (newUser != null) {
                    emitter.onSuccess(
                        LoginDescriptor(
                            credentials.fullName,
                            allowMigration ?: false,
                            oldUserId,
                            newUser.uid
                        )
                    )
                } else {
                    @Suppress("ThrowableNotThrown")
                    emitter.onError(NoUserException())
                }
            }

            val failureHandler: (Throwable) -> Unit = {
                emitter.onError(map(it))
            }

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(credentials.email)
                .addOnSuccessListener { result ->
                    val methods = result.signInMethods
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (!methods.isNullOrEmpty() && currentUser != null) {
                        // The user exists.
                        // There is a currently logged-in user.
                        if (currentUser.isAnonymous) {
                            if (allowMigration == null) {
                                emitter.onError(MigrationRequiredException(credentials))
                                return@addOnSuccessListener
                            }

                            oldUserId = currentUser.uid

                            // The currently logged-in user is anonymous.
                            // We'll delete the anonymous account and login with the new account.
                            currentUser.delete()
                                .addOnSuccessListener {
                                    signInWithCredentials(
                                        firebaseCredentials,
                                        successHandler,
                                        failureHandler
                                    )
                                }
                                .addOnFailureListener {
                                    emitter.onError(it)
                                }
                        } else {
                            // The logged-in user is not anonymous.
                            // We'll try to link this authentication method to the existing account.
                            currentUser.linkWithCredential(firebaseCredentials)
                                .addOnSuccessListener {
                                    successHandler()
                                }
                                .addOnFailureListener {
                                    failureHandler(it)
                                }
                        }
                    } else if (currentUser != null) {
                        // The user does not exist.
                        // There is a logged-in user.
                        // We'll try to link the new authentication method to the existing account.
                        currentUser.linkWithCredential(firebaseCredentials)
                            .addOnSuccessListener {
                                successHandler()
                            }
                            .addOnFailureListener {
                                failureHandler(it)
                            }
                    } else {
                        // This user does not exist.
                        // There's nobody logged-in.
                        // We'll go ahead and sign in with the authentication method.
                        signInWithCredentials(firebaseCredentials, successHandler, failureHandler)
                    }
                }
        }

        return s.flatMap { loginDescriptor ->
            if (updateUserDisplayName && !loginDescriptor.fullName.isNullOrBlank()) {
                return@flatMap updateUser {
                    it.displayName = loginDescriptor.fullName
                    return@updateUser it
                }.andThen(Single.just(loginDescriptor))
            }
            return@flatMap Single.just(loginDescriptor)
        }
    }

    private fun signInWithCredentials(
        credentials: AuthCredential,
        successHandler: () -> Unit,
        failureHandler: (Throwable) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithCredential(credentials)
            .addOnSuccessListener {
                successHandler()
            }
            .addOnFailureListener {
                failureHandler(it)
            }
    }

    override fun logout(resetToAnonymous: Boolean): Completable =
        Completable.defer {
            if (resetToAnonymous && isAnonymous) return@defer Completable.error(
                AlreadyAnonymousException()
            )

            var logoutAction = Completable.create { emitter ->
                try {
                    FirebaseAuth.getInstance().signOut()
                    emitter.onComplete()
                } catch (error: Throwable) {
                    emitter.onError(error)
                }
            }

            if (resetToAnonymous) {
                logoutAction = logoutAction
                    .andThen(loginAnonymously())
            }

            return@defer logoutAction
        }

    override fun updateUser(user: UserData): Completable =
        Completable.defer {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return@defer Completable.error(NoUserException())

            return@defer Completable.create { emitter ->
                val changeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(user.displayName)
                    .build()

                currentUser.updateProfile(changeRequest)
                    .addOnSuccessListener {
                        forceRefreshAutoUpdatingUser.onNext(0)
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.onError(map(it))
                    }
            }
        }

    override fun updateUser(userConfigurationHandler: (UserData) -> UserData): Completable =
        Completable.defer {
            if (FirebaseAuth.getInstance().currentUser == null) return@defer Completable.error(
                NoUserException()
            )

            return@defer autoupdatingUser
                .take(1)
                .filter { it != null }.map { it!! }
                .map(userConfigurationHandler)
                .flatMapCompletable { updateUser(it) }
        }

    override fun updateEmail(newEmail: CharSequence): Completable =
        Completable.defer {
            val currentUser =
                FirebaseAuth.getInstance().currentUser ?: return@defer Completable.error(
                    NoUserException()
                )

            return@defer Completable.create { emitter ->
                currentUser.updateEmail(newEmail.toString())
                    .addOnSuccessListener {
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.onError(map(it))
                    }
            }
        }

    override fun confirmAuthentication(email: CharSequence, password: CharSequence): Completable =
        confirmAuthenticationWithCredentials(
            LoginCredentials(
                "",
                null,
                null,
                email.toString(),
                password.toString(),
                Provider.Password,
                null
            )
        )

    override fun confirmAuthenticationWithCredentials(loginCredentials: LoginCredentials): Completable =
        Completable.create { emitter ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                emitter.onError(NoUserException()); return@create
            }

            currentUser.reauthenticate(loginCredentials.toAuthCredentials())
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener {
                    emitter.onError(map(it))
                }
        }

    override fun deleteUser(resetToAnonymous: Boolean): Completable =
        Completable.defer {
            val currentUser =
                FirebaseAuth.getInstance().currentUser ?: return@defer Completable.error(
                    NoUserException()
                )

            var deleteAction = Completable.create { emitter ->
                currentUser.delete()
                    .addOnSuccessListener {
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.onError(map(it))
                    }
            }

            if (resetToAnonymous) {
                deleteAction = deleteAction.andThen(loginAnonymously())
            }

            return@defer deleteAction
        }

    override fun updatePassword(newPassword: CharSequence): Completable =
        Completable.defer {
            val currentUser =
                FirebaseAuth.getInstance().currentUser ?: return@defer Completable.error(
                    NoUserException()
                )

            if (user!!.authenticationProviders.contains(Provider.Password)) {
                return@defer Completable.create { emitter ->
                    currentUser.updatePassword(newPassword.toString())
                        .addOnSuccessListener {
                            emitter.onComplete()
                        }.addOnFailureListener {
                            emitter.onError(map(it))
                        }
                }
            }

            val email = user?.email ?: return@defer Completable.error(InvalidEmailException())
            return@defer linkUserToEmailAndPassword(currentUser, email, newPassword)
        }

    private fun signInWithAppleHandler(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String
    ): Single<LoginCredentials> {
        val single: Single<LoginCredentials> = Single.create { emitter ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val handler = SignInWithAppleHandler(activity, serviceId, redirectUri)
                loginHandler = handler

                handler.signIn { idToken, nonce, displayName, email, error ->
                    if (error != null) {
                        emitter.onError(error)
                        return@signIn
                    }

                    if (email != null) {
                        emitter.onSuccess(
                            LoginCredentials(
                                idToken ?: "",
                                null,
                                displayName,
                                email,
                                null,
                                Provider.Apple,
                                nonce
                            )
                        )
                    } else {
                        emitter.onError(IllegalArgumentException("Email is null!"))
                    }
                }
            } else {
                emitter.onError(UnsupportedOperationException("Sign in with Apple requires API ${Build.VERSION_CODES.LOLLIPOP}!"))
            }
        }

        return single
            .doOnDispose {
                loginHandler = null
            }
    }

    override fun signInWithApple(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor> =
        signInWithAppleHandler(activity, serviceId, redirectUri)
            .flatMap { loginWithCredentials(it, updateUserDisplayName, allowMigration) }

    override fun confirmAuthenticationWithApple(
        activity: FragmentActivity,
        serviceId: String,
        redirectUri: String
    ): Completable =
        signInWithAppleHandler(activity, serviceId, redirectUri)
            .flatMapCompletable { confirmAuthenticationWithCredentials(it) }

    private fun signInWithGoogleHandler(
        activity: Activity,
        clientId: String,
        requestCode: Int
    ): Single<LoginCredentials> {
        val single: Single<LoginCredentials> = Single.create { emitter ->
            val handler = GoogleSignInHandler(clientId, activity, requestCode)
            loginHandler = handler

            handler.signIn { idToken, _, email, fullName, error ->
                if (error != null) {
                    emitter.onError(error)
                    return@signIn
                }

                if (email != null) {
                    emitter.onSuccess(
                        LoginCredentials(
                            idToken ?: "",
                            null,
                            fullName,
                            email,
                            null,
                            Provider.Google,
                            null
                        )
                    )
                } else {
                    emitter.onError(IllegalArgumentException("Email is null!"))
                }
            }
        }

        return single
            .doOnDispose {
                loginHandler = null
            }
    }

    override fun signInWithGoogle(
        activity: Activity,
        clientId: String,
        requestCode: Int,
        updateUserDisplayName: Boolean,
        allowMigration: Boolean?
    ): Single<LoginDescriptor> =
        signInWithGoogleHandler(activity, clientId, requestCode)
            .flatMap { loginWithCredentials(it, updateUserDisplayName, allowMigration) }

    override fun confirmAuthenticationWithGoogle(
        activity: FragmentActivity,
        clientId: String,
        requestCode: Int
    ): Completable =
        signInWithGoogleHandler(activity, clientId, requestCode)
            .flatMapCompletable { confirmAuthenticationWithCredentials(it) }

    private fun map(error: Throwable): Throwable {
        return error
    }

}
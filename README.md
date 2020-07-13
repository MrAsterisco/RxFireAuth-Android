# RxFireAuth-Android

 [ ![Download](https://api.bintray.com/packages/mrasterisco/RxFireAuth/rxfireauth/images/download.svg) ](https://bintray.com/mrasterisco/RxFireAuth/rxfireauth/_latestVersion)

RxFireAuth is a wrapper around the [Firebase Authentication](https://firebase.google.com/docs/auth) SDK that exposes most of the available functions through [RxKotlin](https://github.com/ReactiveX/RxKotlin) objects. as well as improving the logic around managing and handling accounts throughout the lifecycle of your app.

Firebase Authentication is a great way to support user authentication in your app easily. This library builds on top of that to simplify even further the process with pre-built algorithms that support registering, logging-in, linking accounts with other providers, and more.

Looking for the iOS/iPadOS version? You can find it [right here](https://github.com/MrAsterisco/RxFireAuth).

## Installation

You can install RxFireAuth by adding the RxFireAuth repository to your project's `build.gradle`, under `allprojects > repositories`:

```groovy
maven {
    url  "https://dl.bintray.com/mrasterisco/RxFireAuth" 
}
```

Then add the library as a dependency in your module's `build.gradle`:

```groovy
implementation "io.github.mrasterisco:rxfireauth:$rxfireauth_version"
``` 

To find out the latest version, look at the Releases tab of this repository.

*At the moment, there is an issue with how the library is published which prevents Android Studio from downloading sources, inline documentation and transitive dependencies. If you happen to know what's wrong, please answer [this question on StackOverflow](https://stackoverflow.com/questions/62873155/android-library-on-bintray-missing-sources-and-javadoc) or make a PR to fix the problem right here. In the meantime, you'll have to refer to the [online documentation](https://mrasterisco.github.io/RxFireAuth-Android) and import the required dependencies manually.*

## Get Started
To get started with RxFireAuth, you can download the example project or dive right into the [documentation](https://mrasterisco.github.io/RxFireAuth-Android).

### Example Project
This library includes a sample project that shows how to implement all the functions of the library.

To see it in action, follow these steps:

- Download this repository.
- Navigate to your [Firebase Console](https://console.firebase.google.com/) and create a new project.
- When configuring the project, use `io.github.mrasterisco.rxfireauthexample` as package name.
- Download the `google-services.json` and place it in the `app` folder.
- In the Firebase Console, navigate to Authentication and enable "Email/Password", "Anonymous" and "Google".
- Build and run the app.

*You may need to add your signing certificate SHA1 fingerprint to the Firebase Project. Further info are available on [this Firebase Help page](https://support.google.com/firebase/answer/9137403?hl=en).*

*The example project also supports Sign in with Apple. To configure Sign in with Apple, you must be a member of the [Apple Developer Program](https://developer.apple.com/programs/) and you have to follow some additional steps to configure a Service ID on the Apple Developer portal. For further information, refer to the [Firebase Documentation](https://firebase.google.com/docs/auth/android/apple).*

### References
The whole library is built around the `IUserManager` protocol. The library provides the default implementation of it through the `UserManager` class, that you can instantiate directly or get through Dependency Injection.

### Configuration
RxFireAuth assumes that you have already gone through the [Get Started](https://firebase.google.com/docs/auth/android/start) guide on the Firebase Authentication documentation website. This means that:

- You have already [created a new project](https://firebase.google.com/docs/android/setup#create-firebase-project) in the [Firebase Console](https://console.firebase.google.com/).
- You have [registered your app](https://firebase.google.com/docs/android/setup#register-app) with Firebase and [added the `GoogleService-Info.plist` file](https://firebase.google.com/docs/android/setup#add-config-file).
- You have already [imported the google-services plugin](https://developers.google.com/android/guides/google-services-plugin) *(further info on this step are also available [here](https://firebase.google.com/docs/android/setup#add-config-file))*.
- You have [added the Firebase SDK](https://firebase.google.com/docs/android/setup#add-sdks) to your app.
- You have already turned on and configured the authentication providers that you'd like to use in the Firebase Console.
- If you did everything right, you can now begin to use RxFireAuth.

## Features
RxFireAuth offers several ways to interact with Firebase Authentication in a simple and reactive way.

## Login
One of the things that RxFireAuth aims to simplify is the ability to build a Register/Login screen that works seamlessly for new and returning users, also considering the ability of Firebase to create [anonymous accounts](https://firebase.google.com/docs/auth/android/anonymous-auth).

#### Anonymous Accounts Flow
Modern applications should always try to delay sign-in as long as possible. The Apple Human Interface Guidelines write this, which I believe applies to any app on any platform:

> Delay sign-in as long as possible. People often abandon apps when they're forced to sign in before doing anything useful. Give them a chance to familiarize themselves with your app before committing. For example, a live-streaming app could let people explore available content before signing in to stream something.

Anonymous Accounts are Firebase's way to support this situation: when you first launch the app, you create an anonymous account that can then be converted to a new account when the user is ready to sign-in. This works flawlessly for new accounts but has a few catches when dealing with returning users.

Consider the following situation:

- Mike is a new user of your app. Since you've strictly followed Apple's guidelines when Mike opens your app, he's taken directly to the main screen.
- All the data that Mike builds in your app is linked to an anonymous account that you have created automatically while starting the app for the first time.
- At some point, Mike decides to sign-in to sync his data with another device. He registers a new account with his email and a password.
- Everything's looking good until now with the normal Firebase SDK, **unless you're super into RxSwift and you want all the Firebase methods to be wrapped into Rx components; if that's the case, skip the next points and go directly to "Code Showcase" paragraph.**
- Now, Mike wants to use his shiny new account to sign-in into another device. He downloads the app once again and he finds himself on the Home screen. 
- He goes directly into the Sign-in screen and enters his account credentials: at this point, using the Firebase SDK, you'll try to link the anonymous account that has been created while opening the app to Mike's credential, but you'll get an error saying that those credentials are already in use. **Here's where this library will help you: when logging-in, the `UserManager` class will automatically check if the specified credentials already exist and will use those to login; it'll also delete the anonymous account that is no longer needed and report everything back to you.**

##### Code Showcase
Use the following method to login using an email and a password:

```kotlin
fun login(
    email: CharSequence,
    password: CharSequence,
    allowMigration: Boolean?
): Single<LoginDescriptor>
```

The `allowMigration` parameter is useful in the situation that we've just described: there is an anonymous account that has to be deleted and replace with an existing account. When set to `null`, the library will return a `Single` that emits `MigrationRequiredException` to give your app the chance to ask the user what they'd like to do with the date they have in the anonymous account.

Once the user has made a choice, pass either `true` or `false` to get the same value circled back to your code after the sign in procedure completed successfully.

To support the migration, all sign in methods return an instance of `LoginDescriptor` which gives you the `allowMigration` parameter that you've passed, the User ID of the anonymous account, and the User ID of the account that is now logged-in. With this information, you can go ahead and migrate the data from the anonymous account to the newly logged-in account.

#### Sign-in with Authentication Providers
If you are thinking of providing alternative ways to login into your app, RxFireAuth's got you covered.

When signing in with an external provider, it is always good to just let the user sign in and then figure out later if this is their first time or not. Additionally, it is common practice to let people connect different providers along with their email and password credentials. *Giving people flexibility is always a good choice.*

Let's use the same short story from before, but Mike is now going to use Google Sign In.

- On the first device, nothing changes: with the standard Firebase SDK, we can link the anonymous account with Mike's Google Account.
- On the second device, there are multiple cases to handle:

1. There is an anonymous user logged-in and the Google Account is not linked to any existing account: that's easy! We'll just link the Apple ID with the anonymous user and we're done.
1. There is an anonymous user logged-in, but the Google Account is already linked with another account: we'll have to go through the migration and then sign into the existing account.
1. There is a normal user logged-in and the Google Account is not linked with any other account: the user is trying to link their Google Account with an existing account, let's go ahead and do that.
1. There is a normal user logged-in, but the Google Account is already linked with another account: we'll throw an error because the user must choose what to do.
1. There is nobody logged-in and the Google Account is either already linked or not: we'll sign into the existing or new account.

With RxFireAuth's `login` method family, all of these cases are handled *automagically* for you.

##### Code Showcase

**All of these cases** are handled automatically for you by calling:

```kotlin
fun signInWithGoogle(
    activity: Activity,
    clientId: String,
    requestCode: Int,
    updateUserDisplayName: Boolean,
    allowMigration: Boolean?
): Single<LoginDescriptor>
```

or

```kotlin
fun signInWithApple(
    activity: FragmentActivity,
    serviceId: String,
    redirectUri: String,
    updateUserDisplayName: Boolean,
    allowMigration: Boolean?
): Single<LoginDescriptor>
```

You can use the `updateUserDisplayName` parameter to automatically set the Firebase User `displayName` property to the full name associated with the provider account. *Keep in mind that some providers, such as Apple, allow the user to change this information while signing in for the first time and may return it for new users only that have never signed into your app before.*

This function will behave as the normal sign in, returning `MigrationRequiredException`, if an anonymous account is going to be deleted and `allowMigration` is not set. When this happens, you can use the following function to continue signing in after asking the user what they'd like to do:

```kotlin
fun loginWithCredentials(
    credentials: LoginCredentials,
    updateUserDisplayName: Boolean,
    allowMigration: Boolean?
): Single<LoginDescriptor>
```

The login credentials are embedded in the `MigrationRequiredException` instance and, except for particular cases, you shouldn't need to inspect them.

###### Sign in with Apple
If your app has an iOS version and it supports multiple authentication providers, chances are that you also have Sign in with Apple. Your users may want to use their Apple ID to sign in on Android as well. RxFireAuth **supports Sign in with Apple on Android** using a custom web view that intercepts the redirect that Apple does to the Firebase page.

Before using Sign in with Apple with RxFireAuth, you must configure Sign in with Apple on the Apple Developer Portal. Further information are available on the [Firebase Docs](https://firebase.google.com/docs/auth/android/apple#configure-sign-in-with-apple).

Even though Firebase support Sign in with Apple out-of-the-box (through `startActivityForSignInWithProvider`), RxFireAuth cannot rely on that method to be able to handle all the situations described above. At the moment, the only reasonable solution is a custom WebView. Users may feel unsafe inputting their credentials into a custom WebView in your app, so you might be better off showing a dialog explaining what is going to happen, before beginning the Sign in with Apple procedure for the first time.

There is an alternative implementation that can work with Chrome Custom Tabs and it is already included in this library: you can find it in the `functions` folder. **This alternative is not tested and may be removed at some point in the future**. There are several reasons for this:

1. It relies on Firebase Cloud Functions, but they are going to be removed from the Firebase free plan sometime over the next couple of months. If your app is not already on the paid plan, your functions will stop working.
1. Using that function requires you to change the Sign in with Apple configuration on the Apple Developer Portal to redirect to your function instead of the Firebase handler URL, adding a little bit of complexity to the already very complicated Sign in with Apple configuration.
1. You are responsible of deploying and maintaining the code that reads the [POST parameters that Apple returns](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_js/incorporating_sign_in_with_apple_into_other_platforms#3332115): if Apple changes something, your code will break. Firebase should be way more reliable in handling these changes.

Whatever method you decide to use, refer to the [Apple Documentation](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_js/incorporating_sign_in_with_apple_into_other_platforms#3332115) to learn how to embed the Sign in with Apple button on Android.

#### Standard Flow
If you don't want to support anonymous authentication, you can use this library anyway as all of the methods are built to work even when no account is logged-in.

You can make direct calls to:

```kotlin
fun register(email: CharSequence, password: CharSequence): Completable
```

and to:

```kotlin
fun loginWithoutChecking(
    email: CharSequence,
    password: CharSequence,
    allowMigration: Boolean?
): Single<LoginDescriptor>
```

and also to:

```kotlin
fun linkAnonymousAccount(email: CharSequence, password: CharSequence): Completable
```

These and other similar methods bypass the logic around anonymous and existing/non-existing accounts and provide you direct access to the bare Firebase SDK through RxSwift.

## User Data
You can get the profile of the currently logged-in user by calling:

```kotlin
this.userManager.user
```

or by subscribing to:

```kotlin
this.userManager.autoupdatingUser
```

*This Observable will emit new values every time something on the user profile has changed. RxJava does not support emitting null values, so this Observable will always emit a `UserData` instance: you can check if it is valid user or not by calling `isValid` on it.*

Once signed in, you can quickly inspect the authentication providers of the user by cycling through the `authenticationProviders` array of the `UserData` instance. For a list of the supported providers, see the `Provider` enum class.

## Authentication Confirmation
When performing sensitive actions, such as changing the user password, linking new authentication providers or deleting the user account, Firebase will require you to get a new refresh token by forcing the user to login again. RxFireAuth offers convenient methods to confirm the authentication using one the supported providers.

You can confirm the authentication using email and password:

```kotlin
fun confirmAuthentication(email: CharSequence, password: CharSequence): Completable
```

Google Sign In:

```kotlin
fun confirmAuthenticationWithGoogle(
    activity: FragmentActivity,
    clientId: String,
    requestCode: Int
): Completable
```

or Sign in with Apple:

```kotlin
fun confirmAuthenticationWithApple(
    activity: FragmentActivity,
    serviceId: String,
    redirectUri: String
): Completable
```

## Documentation
**Always refer to the `IUserManager` interface in your code, because the `UserManager` implementation may introduce breaking changes over time even if the library major version hasn't changed.**

The interface is fully document, as all the involved classes.

You can find the [autogenerated documentation here](https://mrasterisco.github.io/RxFireAuth-Android).

## Compatibility
RxFireAuth supports **[API Level 21](https://developer.android.com/studio/releases/platforms#5.0) (Android Lollipop) or later** and has the following dependencies:

- `com.google.firebase:firebase-auth-ktx:19.3.2`
- `io.reactivex.rxjava3:rxkotlin:3.0.0`
- `com.auth0.android:jwtdecode:2.0.0`
- `com.google.android.gms:play-services-auth:18.0.0`

RxFireAuth is built using **Kotlin version 1.3.72**.

## Contributions
All contributions to expand the library are welcome. Fork the repo, make the changes you want, and open a Pull Request.

If you make changes to the codebase, I am not enforicing a coding style, but I may ask you to make changes based on how the rest of the library is made.

## Status
This library is under **active development** and it can be considered stable enough to be used in Production.

Even if most of the APIs are pretty straightforward, **they may change in the future**; but you don't have to worry about that, because releases will follow  [Semanting Versioning 2.0.0](https://semver.org).

## License
RxFireAuth is distributed under the MIT license. [See LICENSE](https://github.com/MrAsterisco/RxFireAuth-Android/blob/master/LICENSE) for details.
# RxFireAuth-Android

 [ ![Download](https://api.bintray.com/packages/mrasterisco/RxFireAuth/rxfireauth/images/download.svg?version=1.0.0) ](https://bintray.com/mrasterisco/RxFireAuth/rxfireauth/1.0.0/link)

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

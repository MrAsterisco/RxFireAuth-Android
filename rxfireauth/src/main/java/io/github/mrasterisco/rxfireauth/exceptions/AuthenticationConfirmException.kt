package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.Exception

class AuthenticationConfirmException: Exception("In order to perform this action, you'll have to confirm your credentials by authenticating again.")
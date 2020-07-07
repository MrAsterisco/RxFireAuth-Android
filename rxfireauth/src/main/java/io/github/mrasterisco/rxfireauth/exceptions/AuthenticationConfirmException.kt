package io.github.mrasterisco.rxfireauth.exceptions

import java.lang.Exception
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager

/**
 * The requested action requires a recent call to [IUserManager.confirmAuthentication] or one of the related calls for other providers.
 */
class AuthenticationConfirmException: Exception("In order to perform this action, you'll have to confirm your credentials by authenticating again.")
package io.github.mrasterisco.rxfireauthexample

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding4.widget.textChanges
import io.github.mrasterisco.rxfireauth.exceptions.MigrationRequiredException
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager
import io.github.mrasterisco.rxfireauth.models.LoginCredentials
import io.github.mrasterisco.rxfireauth.models.LoginDescriptor
import io.github.mrasterisco.rxfireauth.models.Provider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.withLatestFrom
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()
    private var progressDialog: ProgressDialog? = null

    private val userManager: IUserManager
        get() = (requireActivity() as MainActivity).userManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onResume() {
        super.onResume()

        userManager.autoupdatingUser
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!it.id.isNullOrEmpty()) {
                    if (it.isAnonymous) {
                        welcome_text.setText(R.string.welcome_anonymous_label)
                        subtitle_text.setText(R.string.welcome_anonymous_subtitle)
                    } else {
                        if (!it.displayName.isNullOrEmpty()) {
                            welcome_text.text =
                                getString(R.string.welcome_name_label, it.displayName)
                        } else {
                            welcome_text.setText(R.string.welcome_generic_label)
                        }
                        subtitle_text.text = getString(R.string.welcome_name_subtitle, it.email)
                    }

                    name_text.setText(it.displayName)
                    providers_text.text =
                        it.authenticationProviders.joinToString(", ") { a -> a.name }
                } else {
                    welcome_text.setText(R.string.welcome_not_logged_label)
                    subtitle_text.setText(R.string.welcome_not_logged_subtitle)
                    providers_text.setText(R.string.providers_not_logged_label)
                }
            }.addTo(compositeDisposable)

        login_edit.textChanges()
            .map { it.isNotBlank() }
            .withLatestFrom(userManager.autoupdatingUser.map { !it.id.isNullOrEmpty() }) { hasEmail, isLoggedIn ->
                return@withLatestFrom if (!hasEmail && !isLoggedIn) {
                    getString(R.string.sign_in_anonymously_title)
                } else if (isLoggedIn && hasEmail) {
                    getString(R.string.link_title)
                } else if (isLoggedIn && !hasEmail) {
                    getString(R.string.insert_email_link_title)
                } else {
                    getString(R.string.sign_in_title)
                }
            }.subscribe {
                sign_in_button.text = it
            }.addTo(compositeDisposable)

        sign_in_button.setOnClickListener(::signIn)
        sign_in_apple_button.setOnClickListener(::signInWithApple)
        sign_in_google_button.setOnClickListener(::signInWithGoogle)
        sign_out_button.setOnClickListener(::signOut)

        update_profile_button.setOnClickListener(::updateProfile)
        change_password_button.setOnClickListener(::changePassword)
        confirm_authentication_button.setOnClickListener(::confirmAuthentication)
        delete_button.setOnClickListener(::deleteAccount)
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }

    private fun signIn(view: View) {
        toggleProgress(true)
        if (login_edit.text.isEmpty()) {
            userManager.loginAnonymously()
                .subscribe({
                    toggleProgress(false)
                }, ::showError).addTo(compositeDisposable)
        } else {
            userManager.login(login_edit.text, password_edit.text, migrationAllowance)
                .subscribe(::handleLoggedIn, ::handleSignInError)
                .addTo(compositeDisposable)
        }
    }

    private fun signInWithApple(view: View) {
        toggleProgress(true)
        userManager.signInWithApple(requireActivity(), true, migrationAllowance)
            .subscribe(::handleLoggedIn, ::handleSignInError)
            .addTo(compositeDisposable)
    }

    private fun signInWithGoogle(view: View) {
        TODO()
    }

    private fun signOut(view: View) {
        toggleProgress(true)
        userManager.logout(reset_anonymous_checkbox.isChecked)
            .subscribe({
                toggleProgress(false)
            }, ::showError).addTo(compositeDisposable)
    }

    private fun updateProfile(view: View) {
        toggleProgress(false)
        userManager.updateUser {
            return@updateUser it.apply { it.displayName = name_text.text.toString() }
        }.subscribe({
            toggleProgress(false)
        }, ::showError).addTo(compositeDisposable)
    }

    private fun changePassword(view: View) {
        toggleProgress(true)
        userManager.autoupdatingUser
            .take(1)
            .filter { !it.id.isNullOrEmpty() }
            .map { it.authenticationProviders }
            .subscribe {
                toggleProgress(false)
                if (it.contains(Provider.Password)) {
                    changeExistingPassword()
                } else {
                    setNewPassword()
                }
            }.addTo(compositeDisposable)
    }

    private fun deleteAccount(view: View) {
        toggleProgress(false)
        userManager.deleteUser(reset_anonymous_checkbox.isChecked)
            .subscribe({
                toggleProgress(false)
            }, ::showError)
            .addTo(compositeDisposable)
    }

    private fun confirmAuthentication(view: View) {
        val providers = userManager.user?.authenticationProviders ?: emptyList()
        val providerIdentifiers = providers.map { it.identifier }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_authentication_dialog_title)
            .setItems(providerIdentifiers) { dialog, which ->
                dialog.dismiss()
                confirmAuthentication(providers[which])
            }
            .setNegativeButton(R.string.cancel_title) { d, _ -> d.dismiss() }
            .show()
    }

    private fun handleSignInError(error: Throwable) {
        toggleProgress(false)
        if (error is MigrationRequiredException) {
            handleMigration(error.loginCredentials)
        } else {
            showError(error)
        }
    }

    // KEY ID: 72RVVK2PR6

    private fun confirmAuthentication(provider: Provider) {
        when (provider) {
            Provider.Password -> {
                val email = login_edit.text
                val password = password_edit.text

                if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                    showAlert(
                        getString(R.string.insert_email_password_title),
                        getString(R.string.insert_email_password_message)
                    )
                    return
                }

                toggleProgress(true)
                userManager.confirmAuthentication(email, password)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        toggleProgress(false)
                        showAlert(
                            getString(R.string.authentication_confirmed_title),
                            getString(R.string.authentication_confirmed_message)
                        )
                    }, ::showError).addTo(compositeDisposable)
            }
            Provider.Apple -> {
                TODO()
            }
            Provider.Google -> {
                TODO()
            }
        }
    }

    private fun changeExistingPassword() {
        val input = EditText(requireContext())
            .apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                hint = getString(R.string.new_password_hint)
            }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_password_title)
            .setMessage(R.string.new_password_message)
            .setView(input)
            .setPositiveButton(R.string.set_title) { d, _ ->
                d.dismiss()
                userManager.updatePassword(input.text)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showAlert(
                            getString(R.string.password_set_title),
                            getString(R.string.password_set_message)
                        )
                    }, ::showError)
                    .addTo(compositeDisposable)
            }
            .setNegativeButton(R.string.cancel_title) { d, _ -> d.dismiss() }
            .show()
    }

    private fun setNewPassword() {
        val input = EditText(requireContext())
            .apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                hint = getString(R.string.new_password_hint)
            }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_password_title)
            .setMessage(R.string.new_password_message)
            .setView(input)
            .setPositiveButton(R.string.set_title) { d, _ ->
                d.dismiss()
                userManager.updatePassword(input.text)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showAlert(
                            getString(R.string.password_set_title),
                            getString(R.string.password_set_message)
                        )
                    }, ::showError)
                    .addTo(compositeDisposable)
            }
            .setNegativeButton(R.string.cancel_title) { d, _ -> d.dismiss() }
            .show()
    }

    private fun handleMigration(credentials: LoginCredentials?) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.migration_required_title)
            .setMessage(R.string.migration_required_dialog_message)
            .setPositiveButton(R.string.migrate_title) { d, _ ->
                d.dismiss()
                if (credentials != null) {
                    userManager.loginWithCredentials(
                        credentials,
                        updateUserDisplayName = true,
                        allowMigration = true
                    )
                        .subscribe(::handleLoggedIn, ::showError)
                        .addTo(compositeDisposable)
                } else {
                    userManager.login(login_edit.text, password_edit.text, true)
                        .subscribe(::handleLoggedIn, ::showError)
                        .addTo(compositeDisposable)
                }
            }
            .setNegativeButton(R.string.cancel_title) { d, _ -> d.dismiss() }
            .show()
    }

    private fun handleLoggedIn(descriptor: LoginDescriptor) {
        login_edit.text.clear()
        password_edit.text.clear()
        toggleProgress(false)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.login_success_title)
            .setPositiveButton(R.string.cool_title) { d, _ -> d.dismiss() }
            .also {
                val messages = mutableListOf<String>()
                if (descriptor.performMigration) {
                    messages.add(getString(R.string.migration_required_message))
                } else {
                    messages.add(getString(R.string.migration_not_required_message))
                }
                if (descriptor.oldUserId != null) {
                    messages.add(getString(R.string.old_user_id_message, descriptor.oldUserId))
                }
                if (descriptor.newUserId != null) {
                    messages.add(getString(R.string.new_user_id_message, descriptor.newUserId))
                }
                it.setMessage(messages.joinToString(" "))
            }.show()
    }

    private fun toggleProgress(show: Boolean) {
        if (show) {
            progressDialog = ProgressDialog(requireContext())
                .also {
                    it.setTitle(R.string.loading_label)
                    it.show()
                }
        } else {
            progressDialog?.dismiss()
        }
    }

    private fun showAlert(title: CharSequence, message: CharSequence) {
        toggleProgress(false)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(R.string.cancel_title) { d, _ -> d.dismiss() }
            .show()
    }

    private fun showError(error: Throwable) {
        showAlert(getString(R.string.error_dialog_title), error.localizedMessage ?: "")
    }

    private val migrationAllowance: Boolean?
        get() = when (migration_radio_group.checkedRadioButtonId) {
            R.id.migration_radio_allow -> true
            R.id.migration_radio_deny -> false
            else -> null
        }

}

package uniovi.eii.shareit.viewModel

import android.util.Patterns
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.repository.FirebaseAuthService

class LoginViewModel : ViewModel() {

    private val _loginAttempt = MutableLiveData(LoginResult())
    val loginAttempt: LiveData<LoginResult> = _loginAttempt

    fun attemptLogin(email: String, password: String, rememberMe: Boolean) {
        if (!checkValidData(email, password)) return
        viewModelScope.launch(Dispatchers.IO) {
            _loginAttempt.postValue(
                FirebaseAuthService.signInWithEmailAndPassword(email, password)
            )
        }
    }

    fun attemptLoginWithGoogle(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            viewModelScope.launch(Dispatchers.IO) {
                _loginAttempt.postValue(
                    FirebaseAuthService.signInWithGoogle(googleIdTokenCredential.idToken)
                )
            }
        } else {
            _loginAttempt.value = LoginResult(firebaseError = "Credential is not of type Google ID!")
        }
    }

    private fun checkValidData(email: String, password: String): Boolean {
        if (email.isBlank()) {
            _loginAttempt.value = LoginResult(emailError = R.string.err_empty_field)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginAttempt.value = LoginResult(emailError = R.string.err_not_an_email)
            return false
        }
        if (password.isBlank()) {
            _loginAttempt.value = LoginResult(passwordError = R.string.err_empty_field)
            return false
        }
        return true
    }

    data class LoginResult(
        var isUserLogged: Boolean = false,
        var firebaseError: String? = null,
        var emailError: Int? = null,
        var passwordError: Int? = null
    )
}
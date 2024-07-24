package uniovi.eii.shareit.viewModel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R

class LoginViewModel : ViewModel() {

    private val _loginAttempt = MutableLiveData(LoginResult())
    val loginAttempt: LiveData<LoginResult> = _loginAttempt

    fun attemptLogin(email: String, password: String, rememberMe: Boolean) {
        if (!checkValidData(email, password)) return
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: login
            delay(2000)
            _loginAttempt.postValue(LoginResult(true))
        }

    }

    fun logOut() {
        _loginAttempt.value = LoginResult()
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
        var emailError: Int? = null,
        var passwordError: Int? = null
    )
}
package uniovi.eii.shareit.viewModel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import java.util.regex.Pattern

// Passwords must have at least six digits and include
// one digit, one lower case letter and one upper case letter
const val MIN_PASS_LENGTH = 6
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{$MIN_PASS_LENGTH,}$"

class SignUpViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val _signUpAttempt = MutableLiveData(SignUpResult())
    val signUpAttempt: LiveData<SignUpResult> = _signUpAttempt

    fun attemptSignUp(email: String, password: String, passwordRepeat: String) {
        if (!checkValidData(email, password, passwordRepeat)) return
        viewModelScope.launch(Dispatchers.IO) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("SignUp", "createUserWithEmail:success")
                        auth.signOut()
                        _signUpAttempt.postValue(SignUpResult(true))
                    } else {
                        Log.w("SignUp", "createUserWithEmail:failure", it.exception)
                        _signUpAttempt.postValue(SignUpResult(firebaseError = it.exception?.message))
                    }
                }
        }
    }

    private fun checkValidData(email: String, password: String, passwordRepeat: String): Boolean {
        if (email.isBlank()) {
            _signUpAttempt.value = SignUpResult(emailError = R.string.err_empty_field)
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _signUpAttempt.value = SignUpResult(emailError = R.string.err_not_an_email)
            return false
        }
        if (password.isBlank()) {
            _signUpAttempt.value = SignUpResult(passwordError = R.string.err_empty_field)
            return false
        }
        if (password.length < MIN_PASS_LENGTH) {
            _signUpAttempt.value = SignUpResult(passwordError = R.string.err_password_too_short)
            return false
        }
        if (!Pattern.compile(PASS_PATTERN).matcher(password).matches()) {
            _signUpAttempt.value = SignUpResult(passwordError = R.string.err_invalid_password)
            return false
        }
        if (password != passwordRepeat) {
            _signUpAttempt.value = SignUpResult(passwordRepeatError = R.string.err_passwords_dont_match)
            return false
        }
        return true
    }

    data class SignUpResult(
        var isUserCreated: Boolean = false,
        var firebaseError: String? = null,
        var emailError : Int? = null,
        var passwordError : Int? = null,
        var passwordRepeatError: Int? = null
    )
}
package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.User

private const val MAX_NAME_LENGTH = 20

class ProfileViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _currentUser = MutableLiveData(User("1", "Abel", "hola@gmail.com"))
    val currentUser: LiveData<User> = _currentUser

    private val _dataValidation = MutableLiveData<ValidationResult>()
    val dataValidation: LiveData<ValidationResult> = _dataValidation

    fun loadUserProfile() {
        _currentUser.value = User("1", "Abel", "hola@gmail.com")
    }

    fun wipeErrors() {
        _dataValidation.value = ValidationResult(true)
    }

    fun updateData(name: String, email: String, image: String) {
        if (validateData(name)){
            _dataValidation.value = ValidationResult(true)
            _currentUser.value = User("1", name, email, image)
        }
    }

    private fun validateData(name: String): Boolean {
        if (name.isBlank()) {
            _dataValidation.value = ValidationResult(userError = R.string.err_empty_field)
            return false
        }
        if (name.length > MAX_NAME_LENGTH) {
            _dataValidation.value = ValidationResult(userError = R.string.err_name_too_long)
            return false
        }
        return true
    }

    data class ValidationResult(
        var isDataValid: Boolean = false, var userError: Int? = null
    )
}
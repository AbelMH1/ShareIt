package uniovi.eii.shareit.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.R
import uniovi.eii.shareit.model.User
import uniovi.eii.shareit.model.repository.FirestoreUserService

private const val MAX_NAME_LENGTH = 20

class ProfileViewModel : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _dataValidation = MutableLiveData<ValidationResult>()
    val dataValidation: LiveData<ValidationResult> = _dataValidation

    private var userDataListenerRegistration: ListenerRegistration? = null

    private fun updateUserData(newUserData: User) {
        _currentUser.postValue(newUserData)
    }

    fun registerUserDataListener(userId: String) {
        Log.d(TAG, "userDataListener: START")
        val updateEvent: (newData: User) -> Unit = {
            updateUserData(it)
        }
        userDataListenerRegistration =
            FirestoreUserService.getUserDataRegistration(userId, updateEvent)
    }

    fun unregisterUserDataListener() {
        Log.d(TAG, "userDataListener: STOP")
        userDataListenerRegistration?.remove()
    }

    fun attemptDataUpdate(name: String, email: String, image: Uri?) {
        if (!validateData(name)) return
        val newUserData = getChangedData(name, email, image)
        viewModelScope.launch(Dispatchers.IO) {
            _dataValidation.postValue(
                FirestoreUserService.updateCurrentUserData(_currentUser.value!!.userId, newUserData)
            )
        }
    }

    fun wipeErrors() {
        _dataValidation.value = ValidationResult(true)
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

    private fun getChangedData(name: String, email: String, image: Uri?): HashMap<String, Any> {
        with(_currentUser.value!!) {
            val newUserData = this.getChanges(User(userId, name, email))
            if (image != null) {
                newUserData["imagePath"] = image
            }
            Log.d(TAG, "DATACHANGED: $newUserData")
            return newUserData
        }
    }

    data class ValidationResult(
        var isDataValid: Boolean = false,
        var firestoreError: String? = null,
        var userError: Int? = null
    )
}
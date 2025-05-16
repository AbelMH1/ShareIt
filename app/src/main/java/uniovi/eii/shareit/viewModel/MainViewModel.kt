package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.repository.FirebaseAuthService

class MainViewModel : ViewModel() {

    private val _loggedUser = MutableLiveData(FirebaseAuthService.getCurrentUser())
    val loggedUser: LiveData<FirebaseUser?> = _loggedUser

    fun logIn() {
        _loggedUser.value = FirebaseAuthService.getCurrentUser()
    }

    fun logOut() {
        viewModelScope.launch {
            _loggedUser.postValue(FirebaseAuthService.signOut())
        }
    }
}
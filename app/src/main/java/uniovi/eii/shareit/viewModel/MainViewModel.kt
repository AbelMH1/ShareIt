package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import uniovi.eii.shareit.model.repository.FirebaseAuthService

class MainViewModel : ViewModel() {

    private val _loggedUser = MutableLiveData(FirebaseAuthService.getCurrentUser())
    val loggedUser: LiveData<FirebaseUser?> = _loggedUser

    fun logIn() {
        _loggedUser.value = FirebaseAuthService.getCurrentUser()
    }

    fun logOut() {
        _loggedUser.value = FirebaseAuthService.signOut()
    }
}
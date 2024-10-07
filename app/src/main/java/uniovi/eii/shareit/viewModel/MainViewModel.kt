package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val _loggedUser = MutableLiveData(auth.currentUser)
    val loggedUser: LiveData<FirebaseUser?> = _loggedUser

    fun logIn() {
        _loggedUser.value = auth.currentUser
    }
    fun logOut() {
        auth.signOut()
        _loggedUser.value = null
    }
}
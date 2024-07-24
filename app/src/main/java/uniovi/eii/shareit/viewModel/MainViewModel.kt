package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _isUserLogged = MutableLiveData(false)
    val isUserLogged: LiveData<Boolean> = _isUserLogged

    fun logIn() {
        _isUserLogged.value = true
    }
    fun logOut() {
        _isUserLogged.value = false
    }
}
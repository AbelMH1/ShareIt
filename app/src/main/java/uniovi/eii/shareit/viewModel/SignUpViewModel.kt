package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val _isUserLogged = MutableLiveData(false)
    val isUserLogged: LiveData<Boolean> = _isUserLogged

    fun attemptLogin(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: login
            delay(2000)
            _isUserLogged.postValue(true)
        }

    }
}
package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.repository.FirebaseAuthService
import uniovi.eii.shareit.model.repository.FirestoreImageService

class ImageDetailsViewModel : ViewModel() {
    companion object {
        private const val TAG = "ImageDetailsViewModel"
    }

    private val _isLikedByCurrentUser = MutableLiveData(false)
    val isLikedByCurrentUser: LiveData<Boolean> = _isLikedByCurrentUser

    private var likedListenerRegistration: ListenerRegistration? = null

    private fun updateIsLikedByCurrentUser(newValue: Boolean) {
        _isLikedByCurrentUser.value = newValue
    }

    fun registerLikedListener(
        image: Image
    ) {
        Log.d(TAG, "registerLikedListener: START")
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        val updateEvent: (newData: Boolean) -> Unit = {
            updateIsLikedByCurrentUser(it)
        }
        likedListenerRegistration = FirestoreImageService.getImageLikeRegistration(image, userId, updateEvent)
    }

    fun unregisterLikedListener() {
        Log.d(TAG, "registerLikedListener: STOP")
        likedListenerRegistration?.remove()
    }

    fun likeImage(image: Image) {
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreImageService.createImageLike(image, userId)
        }
    }

    fun unlikeImage(image: Image) {
        val userId = FirebaseAuthService.getCurrentUser()!!.uid
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreImageService.deleteImageLike(image, userId)
        }
    }
}
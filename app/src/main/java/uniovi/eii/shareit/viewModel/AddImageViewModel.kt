package uniovi.eii.shareit.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.repository.FirestoreImageService
import uniovi.eii.shareit.model.repository.FirestoreUserService

class AddImageViewModel : ViewModel() {

    private val _isCompletedImageUpload = MutableLiveData<Boolean>()
    val isCompletedImageUpload: LiveData<Boolean> get() = _isCompletedImageUpload

    private val _imageUri = MutableLiveData<Uri?>(null)
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun uploadImage(albumId: String, albumName: String) {
        val currentUser = FirestoreUserService.getCurrentUserData()
        if (currentUser == null || imageUri.value == null) {
            _isCompletedImageUpload.value = false
            return
        }
        val newImage = Image(
            authorId = currentUser.userId,
            authorName = currentUser.name,
            albumId = albumId,
            albumName = albumName
        )
        viewModelScope.launch(Dispatchers.IO) {
            _isCompletedImageUpload.postValue(
                FirestoreImageService.uploadImage(newImage, imageUri.value!!)
            )
        }
    }

    fun loadImage(uri: Uri) {
        _imageUri.postValue(uri)
    }
}
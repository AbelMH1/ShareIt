package uniovi.eii.shareit.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.repository.FirestoreUserService

class AddImageViewModel : ViewModel() {

    private val _imageUri = MutableLiveData<Uri?>(null)
    val imageUri: LiveData<Uri?> get() = _imageUri

    private val _isServiceRunning = MutableLiveData<Boolean>()
    val isServiceRunning: LiveData<Boolean> get() = _isServiceRunning

    fun loadImage(uri: Uri) {
        _imageUri.postValue(uri)
    }

    fun setServiceRunning(isRunning: Boolean) {
        if (_isServiceRunning.value == isRunning) return
        _isServiceRunning.postValue(isRunning)
    }

    fun getImageToUpload(album: Album): Image? {
        val currentUser = FirestoreUserService.getCurrentUserData()
        if (currentUser == null || imageUri.value == null) {
            return null
        }
        return Image(
            authorId = currentUser.userId,
            authorName = currentUser.name,
            authorImage = currentUser.imagePath,
            albumId = album.albumId,
            albumName = album.name,
            imagePath = imageUri.value.toString()
        )
    }
}
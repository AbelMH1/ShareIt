package uniovi.eii.shareit.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.model.repository.FirestoreImageService
import uniovi.eii.shareit.model.repository.FirestoreUserService
import uniovi.eii.shareit.utils.compressImage
import uniovi.eii.shareit.utils.createTempImageFile
import uniovi.eii.shareit.utils.getSecureUriForFile

class AddImageViewModel(application: Application) : AndroidViewModel(application) {

    private val _isCompletedImageLoad = MutableLiveData<Boolean>()
    val isCompletedImageLoad: LiveData<Boolean> get() = _isCompletedImageLoad
    private val _isCompletedImageUpload = MutableLiveData<Boolean>()
    val isCompletedImageUpload: LiveData<Boolean> get() = _isCompletedImageUpload

    private val _imageUri = MutableLiveData<Uri?>(null)
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun uploadImage(albumId: String) {
        val currentUser = FirestoreUserService.getCurrentUserData()
        if (currentUser == null || imageUri.value == null) {
            _isCompletedImageUpload.value = false
            return
        }
        val newImage = Image(
            authorName = currentUser.name,
            authorId = currentUser.userId,
            albumId = albumId
        )
        viewModelScope.launch(Dispatchers.IO) {
            _isCompletedImageUpload.postValue(
                FirestoreImageService.uploadImage(newImage, imageUri.value!!)
            )
        }
    }

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val outputFile = context.createTempImageFile()
            val processed = context.compressImage(uri, outputFile)
            if (processed) {
                _imageUri.postValue(context.getSecureUriForFile(outputFile))
            } else {
                _isCompletedImageLoad.postValue(false)
            }
        }
    }
}
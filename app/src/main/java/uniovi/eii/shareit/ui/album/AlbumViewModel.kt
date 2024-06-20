package uniovi.eii.shareit.ui.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Image
import uniovi.eii.shareit.ui.album.placeholder.PlaceholderContent

class AlbumViewModel : ViewModel() {

    private val _imageList = MutableLiveData<List<Image>>(emptyList())
    val imageList: LiveData<List<Image>> = _imageList

    init {
        updateImageList(PlaceholderContent.getImagesList(25))
    }

    fun updateImageList(newImages: List<Image>) {
        _imageList.value = newImages
    }
}
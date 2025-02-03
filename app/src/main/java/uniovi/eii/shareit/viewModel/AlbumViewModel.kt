package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Album

class AlbumViewModel : ViewModel() {

    private val _currentAlbum = MutableLiveData(Album())
    val currentAlbum: LiveData<Album> = _currentAlbum

    fun updateCurrentAlbum(albumID: String, albumName: String, albumCoverImage: String) {
        _currentAlbum.value = Album(albumID, name = albumName, coverImage = albumCoverImage)
    }

}
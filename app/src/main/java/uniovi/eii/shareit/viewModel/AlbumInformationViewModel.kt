package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Album

class AlbumInformationViewModel : ViewModel() {

    private val _album = MutableLiveData(Album())
    val album: LiveData<Album> = _album

    fun updateCurrentAlbum(albumID: String, albumName: String, albumCoverImage: String) {
        _album.value = Album(albumID, albumName, albumCoverImage)
    }

    fun saveAlbumInfo(newAlbum: Album) {
        if (newAlbum != album.value) _album.value = newAlbum
    }

    fun getAlbumInfo(): Album {
        return album.value?.copy() ?: Album()
    }

    fun hasDisabledShared(newAlbum: Album): Boolean {
        return (album.value?.shared ?: false) && !newAlbum.shared
    }

}
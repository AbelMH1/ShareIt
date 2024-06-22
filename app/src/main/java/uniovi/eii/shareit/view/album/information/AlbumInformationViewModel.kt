package uniovi.eii.shareit.view.album.information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Album

class AlbumInformationViewModel : ViewModel() {

    private val _album = MutableLiveData(Album())
    val album: LiveData<Album> = _album

    fun saveAlbumInfo(newAlbum: Album) {
        if (newAlbum != album.value) _album.value = newAlbum
    }

    fun getAlbumInfo(): Album {
        return album.value?.copy() ?: Album()
    }

    fun hasDisabledShared(newAlbum: Album) : Boolean {
        return (album.value?.shared ?: false) && !newAlbum.shared
    }

}
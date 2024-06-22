package uniovi.eii.shareit.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.view.album.placeholder.PlaceholderContent

class HomeViewModel : ViewModel() {
    private val _albumList = MutableLiveData<List<Album>>(emptyList())
    val albumList: LiveData<List<Album>> = _albumList

    init {
        updateAlbumList(PlaceholderContent.getAlbumList(25))
    }

    fun updateAlbumList(newAlbums: List<Album>) {
        _albumList.value = newAlbums
    }
}
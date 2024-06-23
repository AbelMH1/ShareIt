package uniovi.eii.shareit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uniovi.eii.shareit.model.Album
import uniovi.eii.shareit.model.Section
import uniovi.eii.shareit.view.album.placeholder.PlaceholderContent

class HomeViewModel : ViewModel() {
    private val _albumList = MutableLiveData<List<Album>>(emptyList())
    val albumList: LiveData<List<Album>> = _albumList

    private val _sectionList = MutableLiveData<List<Section>>(emptyList())
    val sectionList: LiveData<List<Section>> = _sectionList

    init {
        updateAlbumList(PlaceholderContent.getAlbumList(25))
        updateSectionList(PlaceholderContent.getSectionList(10))
    }

    fun updateAlbumList(newAlbums: List<Album>) {
        _albumList.value = newAlbums
    }

    fun updateSectionList(newSections: List<Section>) {
        _sectionList.value = newSections
    }
}
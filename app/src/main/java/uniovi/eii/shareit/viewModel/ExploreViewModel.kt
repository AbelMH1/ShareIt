package uniovi.eii.shareit.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniovi.eii.shareit.model.UserAlbum
import uniovi.eii.shareit.model.repository.FirestoreAlbumService

class ExploreViewModel : ViewModel() {

    companion object {
        private const val TAG = "ExploreViewModel"
    }

    private var _isSearchMoreEnabled = MutableLiveData(true)
    val isSearchMoreEnabled: LiveData<Boolean> = _isSearchMoreEnabled
    private var _displayAlbumList = MutableLiveData<List<UserAlbum>>(emptyList())
    val displayAlbumList: LiveData<List<UserAlbum>> = _displayAlbumList

    private var _searchMode = false

    private var _lastAlbumRetrieved: DocumentSnapshot? = null
    private var _albumList: List<UserAlbum> = emptyList()

    private var _lastAlbumSearchRetrieved: DocumentSnapshot? = null
    private var _albumSearchList: List<UserAlbum> = emptyList()
    private var _lastQuery: String = ""

    fun loadMoreAlbums() {
        if (_searchMode) {
            loadMoreSearchAlbums()
        } else {
            loadMorePublicAlbums()
        }
    }

    fun updateAreMoreAlbumsAvailable(areMore: Boolean) {
        Log.d(TAG, "updateAreMoreAlbumsAvailable: $areMore")
        _isSearchMoreEnabled.postValue(areMore)
    }

    fun addNewAlbumBatch(newAlbums: List<UserAlbum>) {
        Log.d(TAG, "addNewAlbumBatch: $newAlbums")
        val newAlbumList = _albumList.plus(newAlbums)
        _albumList = newAlbumList
        _displayAlbumList.postValue(newAlbumList)
    }

    fun updateLastAlbumRetrieved(lastAlbum: DocumentSnapshot) {
        Log.d(TAG, "updateLastAlbumRetrieved: $lastAlbum")
        _lastAlbumRetrieved = lastAlbum
    }

    fun loadInitialData() {
        if (_albumList.isNotEmpty()) return
        Log.d(TAG, "loadInitialData")
        _isSearchMoreEnabled.value = true
        loadMorePublicAlbums()
    }

    private fun loadMorePublicAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.getPublicAlbums(
                _lastAlbumRetrieved,
                ::addNewAlbumBatch,
                ::updateAreMoreAlbumsAvailable,
                ::updateLastAlbumRetrieved
            )
        }
    }

    fun restoreExploreList() {
        if (!_searchMode) return
        _searchMode = false
        _lastQuery = ""
        _displayAlbumList.value = _albumList
        if (_albumList.size >= FirestoreAlbumService.PAGE_SIZE)
            _isSearchMoreEnabled.value = true
    }

    fun updateLastAlbumSearchRetrieved(lastAlbum: DocumentSnapshot) {
        Log.d(TAG, "updateLastAlbumSearchRetrieved: $lastAlbum")
        _lastAlbumSearchRetrieved = lastAlbum
    }

    fun addNewAlbumSearchBatch(newAlbums: List<UserAlbum>) {
        Log.d(TAG, "addNewAlbumSearchBatch: $newAlbums")
        val newAlbumList = _albumSearchList.plus(newAlbums)
        _albumSearchList = newAlbumList
        _displayAlbumList.postValue(newAlbumList)
    }

    fun loadInitialSearchData(query: String) {
        if (query == _lastQuery || query.isBlank()) return
        Log.d(TAG, "loadInitialSearchData: $query")
        _isSearchMoreEnabled.value = true
        _searchMode = true
        _lastAlbumSearchRetrieved = null
        _lastQuery = query
        _albumSearchList = emptyList()
        loadMoreSearchAlbums()
    }

    private fun loadMoreSearchAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreAlbumService.getPublicAlbumsSearch(
                _lastQuery,
                _lastAlbumSearchRetrieved,
                ::addNewAlbumSearchBatch,
                ::updateAreMoreAlbumsAvailable,
                ::updateLastAlbumSearchRetrieved
            )
        }
    }
}
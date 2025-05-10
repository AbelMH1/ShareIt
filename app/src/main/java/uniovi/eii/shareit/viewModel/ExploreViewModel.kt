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

        private val albums = (1..23)
            .map { UserAlbum("$it", "$it", "Author $it", "Album $it") }
    }
    private var _lastAlbumRetrieved: DocumentSnapshot? = null
    private var _areMoreAlbumsAvailable = true

    private var _albumList = MutableLiveData<List<UserAlbum>>(emptyList())
    val albumList: LiveData<List<UserAlbum>> = _albumList
    private var _isSearchMoreEnabled = MutableLiveData(true)
    val isSearchMoreEnabled: LiveData<Boolean> = _isSearchMoreEnabled

    init {
        // Initialize the album list with some mocked data
        _albumList.value = albums.subList(0, 10)
    }

    fun updateAreMoreAlbumsAvailable(areMore: Boolean) {
        Log.d(TAG, "updateAreMoreAlbumsAvailable: $areMore")
        _areMoreAlbumsAvailable = areMore
        _isSearchMoreEnabled.postValue(areMore)
    }

    fun updateLastAlbumRetrieved(lastAlbum: DocumentSnapshot) {
        Log.d(TAG, "updateLastAlbumRetrieved: $lastAlbum")
        _lastAlbumRetrieved = lastAlbum
    }

    fun addNewAlbumBatch(newAlbums: List<UserAlbum>) {
        Log.d(TAG, "addNewAlbumBatch: $newAlbums")
        _albumList.postValue(_albumList.value!!.plus(newAlbums))
    }

    fun loadInitialData() {
        _isSearchMoreEnabled.value = true
        _areMoreAlbumsAvailable = true
        if (_albumList.value!!.isNotEmpty()) return
        loadMoreAlbums()
    }

    fun loadMoreAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            Thread.sleep(2000)
            FirestoreAlbumService.getPublicAlbums(
                _lastAlbumRetrieved,
                ::addNewAlbumBatch,
                ::updateAreMoreAlbumsAvailable,
                ::updateLastAlbumRetrieved
            )
        }
    }
}